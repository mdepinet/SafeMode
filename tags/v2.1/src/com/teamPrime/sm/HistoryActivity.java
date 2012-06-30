/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teamPrime.sm.history.DateItem;
import com.teamPrime.sm.history.DialogCreator;
import com.teamPrime.sm.history.HistoryItem;

public class HistoryActivity extends ListActivity {
	private static final int STARTING_CALLBACK_DIALOG_ID = -1;
	private static final String SHARED_PREF_NAME = "SafeMode - History";
	private static final String FILE_NAME_PREFIX = "HistoryItem ";
	
	private HistoryAdapter historyAdapter;
	private static List<HistoryItem> items;
	private static final HistoryItem emptyItem = new HistoryItem(null,null){
		private static final long serialVersionUID = -7634608861370557155L;
		public String toString(){
			return "Your history is empty.";
		}
		public String getTime(){
			return "";
		}
		public String getTitle(){
			return toString();
		}
		public String getDescription(){
			return "";
		}
	};
	
	private DialogCreator nextDialogCreator = null;
	private int nextDialogSubId = -1;
	private int callbackDialogCounter = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		new PopulateHistoryTask(this).execute((Void[])null);
	}
	/*package*/ void setUpAdapter(){
		historyAdapter = new HistoryAdapter(this, R.layout.history_row, items);
		setListAdapter(historyAdapter);
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.history, menu);
    	return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clearHistory:
            	SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
            	Editor edit = data.edit();
            	edit.putInt("numItems", 0);
            	edit.commit();
            	historyAdapter.clear();
            	historyAdapter.add(emptyItem);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id){
    	if (id < 0){
    		Dialog d = (nextDialogCreator == null ? null : nextDialogCreator.createDialog(this, nextDialogSubId));
    		nextDialogCreator = null;
    		nextDialogSubId = -1;
    		return d;
    	}
    	else return null;
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		HistoryItem hi = historyAdapter.getItem(position);
		hi.onClick();
	}
	
	/*package*/ void populateItems(){
		SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		items = Collections.synchronizedList(new ArrayList<HistoryItem>());
		int numItems = data.getInt("numItems", 0);
		for (int i = 1; i<=numItems; i++){
			ObjectInputStream ois = null;
			FileInputStream fis = null;
			try{
				fis = openFileInput(FILE_NAME_PREFIX+i);
				byte[] b = new byte[1024]; //These better be <= 1KB...
				fis.read(b);
				ByteArrayInputStream bais = new ByteArrayInputStream(b);
				if (bais.available()>0){
					ois = new ObjectInputStream(bais);
					HistoryItem hi = (HistoryItem) ois.readObject();
					hi.setActivity(this);
					items.add(hi);

				}
			} catch (ClassNotFoundException ex){
				Log.e("SafeMode","Failed to load items",ex);
			} catch (StreamCorruptedException ex) {
				Log.e("SafeMode","Failed to load items",ex);
			} catch (IOException ex) {
				Log.e("SafeMode","Failed to load items",ex);
			} finally {
				try{fis.close();ois.close();} catch (Throwable t){}
			}
		}
		sortAndDateItems();
		if (items.isEmpty()) items.add(emptyItem);
	}
	
	private void sortAndDateItems(){
		Collections.sort(items,new Comparator<HistoryItem>(){
			@Override
			public int compare(HistoryItem hi1, HistoryItem hi2) {
				return new Long(hi1.getCreationDate().getTime() - hi2.getCreationDate().getTime()).intValue();
			}
		});
		Calendar lastDate = Calendar.getInstance();
		lastDate.setTimeInMillis(0);
		for (int i = 0; i<items.size(); i++){
			HistoryItem hi = items.get(i);
			Calendar creationDate = Calendar.getInstance();
			creationDate.setTime(hi.getCreationDate());
			creationDate.set(Calendar.HOUR_OF_DAY, 0);
			creationDate.set(Calendar.MINUTE, 0);
			creationDate.set(Calendar.SECOND, 0);
			creationDate.set(Calendar.MILLISECOND, 0);
			if (lastDate.getTimeInMillis() == 0 || lastDate.before(creationDate)){
				DateItem dateLabel = new DateItem(null,null);
				lastDate = creationDate;
				dateLabel.setCreationDate(hi.getCreationDate());
				items.add(i++,dateLabel);
			}
		}
	}
	
	public static int addItem(Context c, HistoryItem hi){
		SharedPreferences data = c.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		int numItems = data.getInt("numItems", 0);
		Editor edit = data.edit();
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		try{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			hi.setActivity(null);
			oos.writeObject(hi);
			fos = c.openFileOutput(FILE_NAME_PREFIX+(++numItems), MODE_PRIVATE);
			fos.write(baos.toByteArray());
			edit.putInt("numItems", numItems);
			edit.commit();
		} catch (IOException ex) {
			Log.e("SafeMode", "Failed to add HistoryItem", ex);
		} finally{
			try{fos.close();oos.close();} catch(Throwable t){}
		}
		return numItems;
	}
	
	public void showDialog(DialogCreator dc, int dialogSubId){
		nextDialogCreator = dc;
		nextDialogSubId = dialogSubId;
		showDialog(STARTING_CALLBACK_DIALOG_ID - callbackDialogCounter++);
	}
	
    private class HistoryAdapter extends ArrayAdapter<HistoryItem> {

        private final List<HistoryItem> items;

        public HistoryAdapter(Context context, int textViewResourceId, List<HistoryItem> items2) {
                super(context, textViewResourceId, items2);
                this.items = items2;
        }
        
        @Override
        public View getView(int position, View view, ViewGroup parent) {
        		RelativeLayout v = null; //Using the view that's passed in causes strange problems (Issue 22)
                TextView dateView;
                TextView title;
                TextView description;
                TextView time;
                
                final HistoryItem historyItem = items.get(position);
                
                if (v == null) {
                    LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if(historyItem instanceof DateItem)
                    	v = (RelativeLayout) inflater.inflate(R.layout.history_date_row, null);
                    else
                    	v = (RelativeLayout) inflater.inflate(R.layout.history_row, null);
                }
                
                if (historyItem != null) {
                	
                	if(historyItem instanceof DateItem){
                		dateView 	= 	(TextView) v.findViewById(R.id.date_entry);
                		if (dateView != null) dateView.setText(historyItem.getDate());
                	}
                	
                	else{
                		
	                    //set title
	                	title = (TextView) v.findViewById(R.id.history_title);
	                    if (title != null) {
	                    	title.setText(historyItem.getTitle());
	                        }
	                         
	                    //set description
	                    description = (TextView) v.findViewById(R.id.history_descrip);
	                    if (description != null){
	                    	description.setText(historyItem.getDescription());
	                   }
	                    	  
	                   	//set time
	                   	time = (TextView) v.findViewById(R.id.history_time);
	                   	if (time != null){
	                   		time.setText(historyItem.getTime());
	                   	}
                	}
                
                }
                return v;
        }
    }

}

class PopulateHistoryTask extends AsyncTask<Void,Void,Void>{
	private HistoryActivity mActivity;
	ProgressDialog loading;
	
	public PopulateHistoryTask(HistoryActivity activity){
		mActivity = activity;
	}

	@Override
    protected void onPreExecute() {
    	loading = ProgressDialog.show(mActivity, "",mActivity.getString(R.string.loading_hist), true); 
    }
	@Override
	protected Void doInBackground(Void... arg0) {
		mActivity.populateItems();
		return null;
	}
	@Override
    protected void onPostExecute(Void result) {
		mActivity.setUpAdapter();
		try{loading.dismiss();}
		catch(Exception ex){}
	}
	
}


