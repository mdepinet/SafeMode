/** This is the main activity for the blacklist
 * 
 * @author Boris Treskunov
 *  
 */

//onSavedInstanceState()

package com.teamPrime.sm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import android.app.ListActivity;
import android.content.SharedPreferences;
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

public class BlackListActivity extends ListActivity {
    /** Called when the activity is first created. */
	
	Button mAddButton;	
	Button mAddAll;
	Button mRemoveAll;
	Button mRemove;
    
	ArrayAdapter<String> mArrayAdapterBL;
	ArrayList<String> blacklistedContacts = new ArrayList<String>();
	AutoCompleteTextView mAutoComplete;
	ArrayAdapter<String> mArrayAdapterAC;
	ArrayList<String> addedContacts = new ArrayList<String>();
	ArrayList<Contact> contacts = new ArrayList<Contact>();
	ArrayList<String> contactNames = new ArrayList<String>(); 
	Map<Long, Triple<String,Integer,String>[]> iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
	Map<Long, Triple<String,Integer,String>[]> iDmapPrev = new TreeMap<Long, Triple <String,Integer,String>[]>();
	Map<Long, Triple<String,Integer,String>[]> iDmapFull = new TreeMap<Long, Triple <String,Integer,String>[]>();
	boolean readOnly = false;
	boolean emptyList = true;
	boolean savedNull = true;
	ArrayList<String> tempContacts = new ArrayList<String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){savedNull = false;}
        readOnly = getIntent().getBooleanExtra("readOnly", true);
        if (readOnly){
        	setContentView(R.layout.readonly);
        	instantiateVariables();
        	restore();
        }
        else{
        	setContentView(R.layout.black_list);
        	instantiateVariables();
        	//mTask = new PopulateTask(this);
        	//mTask.execute((Void[])(null));
        	populatePeopleList();    
        	restore();
 
	        mAddButton.setOnClickListener(new OnClickListener(){    	 	
	        	public void onClick(View v){
	        		String name = mAutoComplete.getText().toString();
	        		if (!contactNames.contains(name))
	        			Toast.makeText(getApplicationContext(), "Incorrect Contact Name!", Toast.LENGTH_SHORT).show();
	        		else if (addedContacts.contains(name))
	        			Toast.makeText(getApplicationContext(), "Contact is Already in Blacklist!", Toast.LENGTH_SHORT).show();
	        		else{
	        			mArrayAdapterBL.add(name);
	        			addedContacts.add(name);
	        			if(emptyList){
		        			mArrayAdapterBL.remove("Your Blacklist is Currently Empty...");
		        			emptyList = false;
		        		}
	        			for(int i = 0; i < contacts.toArray().length; i++){
	        				if(contacts.get(i).match(name))
	        					iDmap.put(Long.parseLong(contacts.get(i).getID()), contacts.get(i).getNumber());
	        			}
	        			//Log.i("safemode", iDmap.toString());
	        		}
	        		Collections.sort(blacklistedContacts);
	        		mAutoComplete.setText("");
	        		}
	        	});
	
	        mAddAll.setOnClickListener(new OnClickListener(){    	 	
	        	public void onClick(View v){	
	        		if(emptyList){
	        			mArrayAdapterBL.remove("Your Blacklist is Currently Empty...");
	        			emptyList = false;
	        		}
	        		int initialSize = addedContacts.size();
	        		for(Contact contact: contacts) {
	        			if (!addedContacts.contains(contact.getName())){
	        				mArrayAdapterBL.add(contact.getName());
	        				addedContacts.add(contact.getName());
	        				}
	        			if (addedContacts.size()==initialSize) 
	        				Toast.makeText(getApplicationContext(), "All Contacts are Already in Blacklist!", Toast.LENGTH_SHORT).show();
	        			iDmap.put(Long.parseLong(contact.getID()), contact.getNumber());
	        			}
	        		//Log.i("safemode", iDmap.toString());
	        		Collections.sort(blacklistedContacts);
	        		mAutoComplete.setText("");
	        		}
	        	});
	
	       	mRemoveAll.setOnClickListener(new OnClickListener(){    	 	
	       		public void onClick(View v){
	        		mArrayAdapterBL.clear();
	        		for (String name:addedContacts){
	        			for(int i = 0; i < contacts.toArray().length; i++){
	        				if(contacts.get(i).match(name))
	        					iDmap.remove(Long.parseLong(contacts.get(i).getID()));
	        			}
	        		}
	        		addedContacts.clear();
	        		Toast.makeText(getApplicationContext(), "All Contacts Removed from Blacklist!", Toast.LENGTH_SHORT).show();
	        		Collections.sort(blacklistedContacts);
	                if(mArrayAdapterBL.isEmpty()){
	                	mArrayAdapterBL.add("Your Blacklist is Currently Empty...");
	                	emptyList = true;
	                }
	        		mAutoComplete.setText("");
	        		}
	        	});
	        	
	       	mRemove.setOnClickListener(new OnClickListener(){    	 	
	       		public void onClick(View v){
	       			String name = mAutoComplete.getText().toString();
	        		if (!addedContacts.contains(name))
	        			Toast.makeText(getApplicationContext(), "Contact is not in Blacklist!", Toast.LENGTH_SHORT).show();
	        		else { 
	       				mArrayAdapterBL.remove(name);
	       				addedContacts.remove(name);
	        			for(int i = 0; i < contacts.toArray().length; i++){
	        				if(contacts.get(i).match(name))
	        					iDmap.remove(Long.parseLong(contacts.get(i).getID()));
	        			}
	       			}
	        		Collections.sort(blacklistedContacts);
	                if(mArrayAdapterBL.isEmpty()){
	                	mArrayAdapterBL.add("Your Blacklist is Currently Empty...");
	                	emptyList = true;
	                }
	        		mAutoComplete.setText("");
	        		}
	        	});
	        }
}
    
    private void instantiateVariables(){
        	blacklistedContacts = new ArrayList<String>();
        	addedContacts = new ArrayList<String>();
        	contacts = new ArrayList<Contact>();
        	contactNames = new ArrayList<String>(); 
        	iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
            mArrayAdapterBL = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blacklistedContacts);
            this.setListAdapter(mArrayAdapterBL);
            iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
    	if (!readOnly){
	    	mAddButton = (Button) findViewById(R.id.add);
	        mAddAll = (Button) findViewById(R.id.add_all_button);
	        mRemoveAll = (Button) findViewById(R.id.remove_all_button);
	        mRemove = (Button) findViewById(R.id.remove);
		      	mArrayAdapterAC = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,contactNames);
		        mAutoComplete = (AutoCompleteTextView)findViewById(R.id.blacklist_text);
		        mAutoComplete.setAdapter(mArrayAdapterAC);
		        mAutoComplete.setThreshold(2);		        
        }
    }

    public void populatePeopleList() {
    	contacts.clear();
    	contactNames.clear();
    	String phoneNumber = "";
    	String phoneLabel = "";
    	Integer phoneType = 0;
    	Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
    
    	while (people.moveToNext()){
	    	String name = people.getString(people.getColumnIndex(
	    	ContactsContract.Contacts.DISPLAY_NAME));
	    	String iD = people.getString(people.getColumnIndex(
	    	ContactsContract.Contacts._ID));
	    	String hasPhone = people.getString(people.getColumnIndex(
	    	ContactsContract.Contacts.HAS_PHONE_NUMBER));

	  		if ((Integer.parseInt(hasPhone) > 0)) {
	  			Cursor numbers = getContentResolver().query(
	  					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ iD, null, null);
	  			ArrayList<Triple<String,Integer,String>> nums = new ArrayList<Triple<String,Integer,String>>();
	  			while (numbers.moveToNext()) {
	  				phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
	  				phoneLabel = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.LABEL));
	  				String str = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.TYPE));
	  				phoneType = str == null ? null : Integer.parseInt(str);
	  				nums.add(new Triple<String,Integer,String>(phoneNumber,phoneType,phoneLabel));
	  			}
	  			contacts.add(new Contact(iD, name, nums));
	  		}
    	}
    	people.close();
    	
    	for (Contact contact: contacts){
    		contactNames.add(contact.getName());
    		iDmapFull.put(Long.parseLong(contact.getID()), contact.getNumber());
    	}
    	//startManagingCursor(people);
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if(!readOnly){
    	super.onListItemClick(l, v, position, id);
		Object o = this.getListAdapter().getItem(position);
		String keyword = o.toString();
		if(keyword.equals("Your Blacklist is Currently Empty...")) return;
		mAutoComplete.setText("");
		mAutoComplete.setText(keyword);
		}
	}
    
    /**
    @Override
    public void onSaveInstanceState(Bundle bundle){
    	super.onSaveInstanceState(bundle);
    	bundle.putBoolean("state", true);
    }
    */    
    
    @Override
    public void onPause(){
        super.onPause();
        int x = 0;
        BlackListIOTask dbTask = new BlackListIOTask(this, iDmapPrev,iDmap,0);
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
    
    @Override
    public void onResume(){
    	super.onResume();
    }

    public void restore(){
    	if(!savedNull){
    	SharedPreferences data = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        int savedInt = data.getInt("length", 0);
        for (int i = 0; i < savedInt;i++){
        	for(int j = 0; j < contacts.toArray().length; j++){
				if(contacts.get(j).match(data.getString("name" + i, "")))
					iDmapPrev.put(Long.parseLong(contacts.get(j).getID()), contacts.get(j).getNumber());
				}
        	mArrayAdapterBL.add(data.getString("name"+i, ""));
        	addedContacts.add(data.getString("name"+i, ""));
    }
    	}   
        if(mArrayAdapterBL.isEmpty()){
        	mArrayAdapterBL.add("Your Blacklist is Currently Empty...");
        	emptyList = true;
        }
}
    
}


/**	
 * this was the code for a popupWindow that may be revived one day...
 * but for now it shall live in comments.
 *
 *SharedPreferences data = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        
        editor.commit();
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		keyword = this.getListAdapter().getItem(position).toString();		
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    remove = new PopupWindow(inflater.inflate(R.layout.list, null, false), 200, 200, true);
	    remove.showAtLocation(this.findViewById(R.id.blacklist_text), Gravity.CENTER, 0, 0);
	    View pview = inflater.inflate(R.layout.list,(ViewGroup)findViewById(R.layout.main));
	    Button mRemove=(Button) pview.findViewById(R.id.remove);
	    
	    mRemove.setOnClickListener(new OnClickListener(){    	 	
    		public void onClick(View v){
    			mArrayAdapter.clear();
    		}
    	});
	    

	}
 *
 */


