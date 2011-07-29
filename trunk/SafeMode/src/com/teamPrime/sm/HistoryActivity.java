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
import java.util.List;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.teamPrime.sm.history.DialogCreator;
import com.teamPrime.sm.history.HistoryItem;

public class HistoryActivity extends ListActivity {
	private static final int CALLBACK_DIALOG_ID = -1;
	private static final String SHARED_PREF_NAME = "SafeMode - History";
	private static final String FILE_NAME_PREFIX = "HistoryItem ";
	
	private ArrayAdapter<HistoryItem> adapter;
	private static List<HistoryItem> items;
	private DialogCreator nextDialogCreator = null;
	private int nextDialogSubId = -1;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		populateItems();
		adapter = new ArrayAdapter<HistoryItem>(this,android.R.layout.simple_list_item_1,items);
		setListAdapter(adapter);
	}
	
	@Override
	public void onPause(){
		super.onPause();
		saveItems();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.hist_clear));
        return result;
    }
    
    @Override
    protected Dialog onCreateDialog(int id){
    	switch (id){
		case CALLBACK_DIALOG_ID:
    		Dialog d = (nextDialogCreator == null ? null : nextDialogCreator.createDialog(this, nextDialogSubId));
    		nextDialogCreator = null;
    		nextDialogSubId = -1;
    		return d;
		default:
			return null;
    	}
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
    	Editor edit = data.edit();
    	edit.putInt("numItems", 0);
    	edit.commit();
    	items.clear();
        return super.onOptionsItemSelected(item);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
		HistoryItem hi = adapter.getItem(position);
		hi.onClick();
	}
	
	private void populateItems(){
		SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		items = new ArrayList<HistoryItem>();
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
	}
	
	private void saveItems(){
		if (items == null || items.isEmpty()) return;
		SharedPreferences data = getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
		Editor edit = data.edit();
		edit.putInt("numItems", items.size());
		int i = 0;
		for (HistoryItem hi : items){
			ObjectOutputStream oos = null;
			FileOutputStream fos = null;
			try{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				oos = new ObjectOutputStream(baos);
				hi.setActivity(null);
				oos.writeObject(hi);
				fos = openFileOutput(FILE_NAME_PREFIX+(++i), MODE_PRIVATE);
				fos.write(baos.toByteArray());
			} catch (IOException ex) {
				ex.printStackTrace();
			} finally{
				try{fos.close(); oos.close();} catch(Throwable t){}
			}
		}
		edit.commit();
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
		showDialog(CALLBACK_DIALOG_ID);
	}
}
