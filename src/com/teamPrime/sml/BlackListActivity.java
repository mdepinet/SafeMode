/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.teamPrime.sml.R;
import com.teamPrime.sml.tasks.BlackListIOTask;
import com.teamPrime.sml.tasks.PopulateTask;

/**
 * This is the main activity for the blacklist
 * 
 * @author Boris Treskunov
 *  
 */
public class BlackListActivity extends ListActivity {
	private static final double MAX_BLACKLIST_SIZE = SafeLaunchActivity.FULL_VERSION ? Double.POSITIVE_INFINITY : 1.0;
	
	private Button mAddButton;	
	private Button mAddAll;
	private Button mRemoveAll;
	private Button mRemove;
    
	private ArrayAdapter<String> mArrayAdapterBL;
	
	private List<String> blacklistedContacts = new LinkedList<String>();
	private List<String> addedContacts = new LinkedList<String>();
	private List<Long> addedContactIds = new LinkedList<Long>();
	
	private AutoCompleteTextView mAutoComplete;
	private ArrayAdapter<String> mArrayAdapterAC;
	
	boolean readOnly = false;
	boolean emptyList = true;
	boolean savedNull = true;
	List<String> tempContacts = new LinkedList<String>();
	PopulateTask mTask;
	
	private List<Long> contactIds = new LinkedList<Long>();
	private List<String> contactNames = new LinkedList<String>();
	private Map<String, Long> nameToIdMap = new TreeMap<String, Long>();

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null) savedNull = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        readOnly = getIntent().getBooleanExtra("readOnly", true);
        if (readOnly){
        	setContentView(R.layout.readonly);
        	instantiateVariables();
        	restore();
        }
        else{
        	setContentView(R.layout.black_list);
        	instantiateVariables();
        	mTask = new PopulateTask(this);
        	mTask.execute((Void[])(null));
        	restore();
 
	        mAddButton.setOnClickListener(new OnClickListener(){    	 	
	        	public void onClick(View v){
	        		if (getArrayAdapter()!=null){
	        		String name = mAutoComplete.getText().toString();
	        		if (!contactNames.contains(name))
	        			Toast.makeText(getApplicationContext(), getString(R.string.bl_err_incor_name), Toast.LENGTH_SHORT).show();
	        		else if (addedContacts.contains(name))
	        			Toast.makeText(getApplicationContext(), getString(R.string.bl_err_contact_present), Toast.LENGTH_SHORT).show();
	        		else{
	        			if (addedContacts.size() < MAX_BLACKLIST_SIZE){
		        			mArrayAdapterBL.add(name);
		        			addedContacts.add(name);
		        			if(emptyList){
			        			mArrayAdapterBL.remove(getString(R.string.bl_list_empty));
			        			emptyList = false;
			        		}
	        			}
	        			else {
	        				Toast.makeText(getApplicationContext(), getString(R.string.bl_err_limit_reached), Toast.LENGTH_SHORT).show();
	        			}
	        		}
	        		Collections.sort(blacklistedContacts);
	        		getAutoComplete().setText("");
	        		}
	        	}
	        });
	
	        mAddAll.setOnClickListener(new OnClickListener(){   	 	
	        	public void onClick(View v){
	        		if (getArrayAdapter()!=null){
		        		if(emptyList){
		        			mArrayAdapterBL.remove(getString(R.string.bl_list_empty));
		        			emptyList = false;
		        		}
		        		int initialSize = addedContacts.size();
		        		for(String name: contactNames) {
		        			if (!addedContacts.contains(name)){
		        				if (addedContacts.size() < MAX_BLACKLIST_SIZE) {
			        				mArrayAdapterBL.add(name);
			        				addedContacts.add(name);
		        				}
		        				else {
		        					Toast.makeText(getApplicationContext(), getString(R.string.bl_err_limit_reached), Toast.LENGTH_SHORT).show();
		        					initialSize = -1; //Just don't print other error
		        					break;
		        				}
		        			}
		        		}
		        		if (addedContacts.size()==initialSize) {
	        				Toast.makeText(getApplicationContext(), getString(R.string.bl_err_all_present), Toast.LENGTH_SHORT).show();
		        		}
		        		Collections.sort(blacklistedContacts);
		        		getAutoComplete().setText("");
	        		}
	        	}
	        });
	
	       	mRemoveAll.setOnClickListener(new OnClickListener(){    	 	
	       		public void onClick(View v){ 
	       			if (getArrayAdapter()!=null){
	       			mArrayAdapterBL.clear();
	        		addedContacts.clear();
	        		Toast.makeText(getApplicationContext(), getString(R.string.bl_state_all_rem), Toast.LENGTH_SHORT).show();
	                if(mArrayAdapterBL.isEmpty()){
	                	mArrayAdapterBL.add(getString(R.string.bl_list_empty));
	                	emptyList = true;
	                }
	        		getAutoComplete().setText("");
	        		}
	       		}
	        });
	        	
	       	mRemove.setOnClickListener(new OnClickListener(){    	 	
	       		public void onClick(View v){
	       			if (getArrayAdapter()!=null){
	       			String name = getAutoComplete().getText().toString();
	        		if (!addedContacts.contains(name))
	        			Toast.makeText(getApplicationContext(), getString(R.string.bl_err_not_present), Toast.LENGTH_SHORT).show();
	        		else { 
	       				mArrayAdapterBL.remove(name);
	       				addedContacts.remove(name);
	       			}
	                if(mArrayAdapterBL.isEmpty()){
	                	mArrayAdapterBL.add(getString(R.string.bl_list_empty));
	                	emptyList = true;
	                }
	        		getAutoComplete().setText("");
	        		}
	       		}
	        });
        }
	}
    
    private void instantiateVariables(){
        	blacklistedContacts = new ArrayList<String>();
        	addedContacts = new LinkedList<String>();
        	addedContactIds = new LinkedList<Long>();
            mArrayAdapterBL = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blacklistedContacts);
            this.setListAdapter(mArrayAdapterBL);
    	if (!readOnly){
	    	mAddButton = (Button) findViewById(R.id.add);
	        mAddAll = (Button) findViewById(R.id.add_all_button);
	        mRemoveAll = (Button) findViewById(R.id.remove_all_button);
	        mRemove = (Button) findViewById(R.id.remove);	        
        }
    }

    public void setArrayAdapter(ArrayAdapter<String> mArrayAdapterAC) {
		this.mArrayAdapterAC = mArrayAdapterAC;
	}

	public ArrayAdapter<String> getArrayAdapter() {
		return mArrayAdapterAC;
	}

	public void setAutoComplete(AutoCompleteTextView mAutoComplete) {
		this.mAutoComplete = mAutoComplete;
	}

	public AutoCompleteTextView getAutoComplete() {
		return mAutoComplete;
	}
	
	public List<String> getContactNames(){
		return contactNames;
	}

	public void populatePeopleList() {
		contactIds.clear();
		contactNames.clear();
    	Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
    			new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER},
    			ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1 AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1",
    			null, ContactsContract.Contacts.DISPLAY_NAME + " DESC");
    
    	while (people.moveToNext()){
	    	String name = people.getString(0);
	    	Long id = people.getLong(1);
	    	contactNames.add(name);
	    	contactIds.add(id);
	    	nameToIdMap.put(name, id);
    	}
    	people.close();
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(!readOnly){
	    	super.onListItemClick(l, v, position, id);
			Object o = this.getListAdapter().getItem(position);
			String keyword = o.toString();
			if(keyword.equals(getString(R.string.bl_list_empty))) return;
			getAutoComplete().setText("");
			getAutoComplete().setText(keyword);
		}
	}   
    
    @Override
    public void onPause(){
        super.onPause();
        saveState();       
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    } 
    
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    @Override
    public void onBackPressed(){
    	super.onBackPressed();
    }

    public void restore(){
    	SharedPreferences data = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        int savedInt = data.getInt("length", 0);
        for (int i = 0; i < savedInt;i++){
        	String nextName = data.getString("name"+i, "");
        	mArrayAdapterBL.add(nextName);
        	addedContacts.add(nextName);
        	addedContactIds.add(nameToIdMap.get(nextName));
    }
        if(mArrayAdapterBL.isEmpty()){
        	mArrayAdapterBL.add(getString(R.string.bl_list_empty));
        	emptyList = true;
        }
}
    
    public void saveState(){
    	int x = 0;
    	addedContactIds.clear();
    	for (String name : addedContacts){
    		Long id = nameToIdMap.get(name);
    		if (id != null) addedContactIds.add(id);
    	}
    	BlackListIOTask dbTask = new BlackListIOTask(this, addedContactIds , BlackListIOTask.WRITE_IDS_MODE);
    	dbTask.execute((Void[])(null));       
        SharedPreferences data = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        for(String name:addedContacts){
        	editor.putString("name" + x,name);
        	x++;
        }
        editor.putInt("length", addedContacts.size());
        editor.commit();
    }
    
}