/** This is the main activity for the blacklist
 * 
 * @author Boris Treskunov
 *  
 */

//onSavedInstanceState()

package com.teamPrime.sm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.R;
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

import com.teamPrime.sm.data.Contact;
import com.teamPrime.sm.tasks.BlackListIOTask;
import com.teamPrime.sm.tasks.PopulateTask;

public class BlackListActivity extends ListActivity {
    /** Called when the activity is first created. */
	
	private Button mAddButton;	
	private Button mAddAll;
	private Button mRemoveAll;
	private Button mRemove;
    
	private ArrayAdapter<String> mArrayAdapterBL;
	
	private List<String> blacklistedContacts = new LinkedList<String>();
	private List<String> addedContacts = new LinkedList<String>();
	private List<Long> addedContactIds = new LinkedList<Long>();
//	private List<Contact> contacts = new LinkedList<Contact>();
	
	private AutoCompleteTextView mAutoComplete;
	private ArrayAdapter<String> mArrayAdapterAC;
	
//	private ArrayList<String> contactNames = new ArrayList<String>(); 
//	Map<Long, Triple<String,Integer,String>[]> iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
//	Map<Long, Triple<String,Integer,String>[]> iDmapPrev = new TreeMap<Long, Triple <String,Integer,String>[]>();
//	Map<Long, Triple<String,Integer,String>[]> iDmapFull = new TreeMap<Long, Triple <String,Integer,String>[]>();
	
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
        	//populatePeopleList();    
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
	        			mArrayAdapterBL.add(name);
	        			addedContacts.add(name);
	        			//addedContactIds.add(nameToIdMap.get(name));
	        			if(emptyList){
		        			mArrayAdapterBL.remove(getString(R.string.bl_list_empty));
		        			emptyList = false;
		        		}
//	        			for(int i = 0; i < contacts.toArray().length; i++){
//	        				if(contacts.get(i).match(name))
//	        					iDmap.put(Long.parseLong(contacts.get(i).getID()), contacts.get(i).getNumber());
//	        			}
	        			//Log.i("safemode", iDmap.toString());
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
	        				mArrayAdapterBL.add(name);
	        				addedContacts.add(name);
	        				//addedContactIds.add(nameToIdMap.get(name));
	        				}
	        			if (addedContacts.size()==initialSize) 
	        				Toast.makeText(getApplicationContext(), getString(R.string.bl_err_all_present), Toast.LENGTH_SHORT).show();
//	        			iDmap.put(Long.parseLong(contact.getID()), contact.getNumber());
	        			}
	        		//Log.i("safemode", iDmap.toString());
	        		Collections.sort(blacklistedContacts);
	        		getAutoComplete().setText("");
	        		}
	        	}
	        	});
	
	       	mRemoveAll.setOnClickListener(new OnClickListener(){    	 	
	       		public void onClick(View v){ 
	       			if (getArrayAdapter()!=null){
	       			mArrayAdapterBL.clear();
//	        		for (String name:addedContacts){
//	        			for(int i = 0; i < contacts.toArray().length; i++){
//	        				if(contacts.get(i).match(name))
//	        					iDmap.remove(Long.parseLong(contacts.get(i).getID()));
//	        			}
//	        		}
	        		addedContacts.clear();
	        		//addedContactIds.clear();
	        		Toast.makeText(getApplicationContext(), getString(R.string.bl_state_all_rem), Toast.LENGTH_SHORT).show();
//	        		Collections.sort(blacklistedContacts);
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
	       				//addedContactIds.remove(nameToIdMap.get(name));
//	        			for(int i = 0; i < contacts.toArray().length; i++){
//	        				if(contacts.get(i).match(name))
//	        					iDmap.remove(Long.parseLong(contacts.get(i).getID()));
//	        			}
	       			}
//	        		Collections.sort(blacklistedContacts);
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
//        	contacts = new ArrayList<Contact>();
//        	contactNames = new ArrayList<String>(); 
//        	iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
//        	iDmapPrev = new TreeMap<Long, Triple <String,Integer,String>[]>();
            mArrayAdapterBL = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blacklistedContacts);
            this.setListAdapter(mArrayAdapterBL);
//            iDmap = new TreeMap<Long, Triple <String,Integer,String>[]>();
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
        //saveState();       
    } 
    
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    @Override
    public void onBackPressed(){
    	//saveState();
    	//onPause();
    	super.onBackPressed();
    }

    public void restore(){
    	//if(!savedNull){
    	SharedPreferences data = getSharedPreferences(getClass().getName(), MODE_PRIVATE);
        int savedInt = data.getInt("length", 0);
        for (int i = 0; i < savedInt;i++){
//        	for(int j = 0; j < contacts.toArray().length; j++){
//				if(contacts.get(j).match(data.getString("name" + i, "")))
//					iDmapPrev.put(Long.parseLong(contacts.get(j).getID()), contacts.get(j).getNumber());
//				}
        	String nextName = data.getString("name"+i, "");
        	mArrayAdapterBL.add(nextName);
        	addedContacts.add(nextName);
        	addedContactIds.add(nameToIdMap.get(nextName));
    }
    	//}    
        if(mArrayAdapterBL.isEmpty()){
        	mArrayAdapterBL.add(getString(bl_list_empty));
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


