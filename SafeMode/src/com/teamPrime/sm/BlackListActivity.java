/** This is the main activity for the blacklist
 * 
 * @author Boris Treskunov
 */


//MAKE SURE TO ADD CONTACTS PERMISSION TO MANIFEST IN MAIN VERSION

package com.teamPrime.sm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class BlackListActivity extends ListActivity {
    /** Called when the activity is first created. */
	
	Button mAddButton;	
	Button mAddAll;
	Button mRemoveAll;
	Button mRemove;
	ArrayAdapter<String> mArrayAdapterBL;
	LinkedList<String> blacklistedContacts;
	AutoCompleteTextView mAutoComplete;
	ArrayAdapter<String> mArrayAdapterAC;
	ArrayList<String> addedContacts;
	ArrayList<Contact> contacts;
	ArrayList<String> contactNames;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_list);
     
        instantiateVariables();
        populatePeopleList();    
 
        mAddButton.setOnClickListener(new OnClickListener(){    	 	
        	public void onClick(View v){
        		String name = mAutoComplete.getText().toString();
        		if (!contactNames.contains(name))
        			Toast.makeText(getApplicationContext(), "Incorrect Contact Name!", Toast.LENGTH_LONG).show();
        		else if (addedContacts.contains(name))
        			Toast.makeText(getApplicationContext(), "Contact is Already in Blacklist!", Toast.LENGTH_LONG).show();
        		else{
        			mArrayAdapterBL.add(name);
        			addedContacts.add(name);
        		}
        		mAutoComplete.setText("");
        		}
        	});

        mAddAll.setOnClickListener(new OnClickListener(){    	 	
        	public void onClick(View v){	
        		int initialSize = addedContacts.size();
        		for(Contact contact: contacts) {
        			if (!addedContacts.contains(contact.getName() + " (" + contact.getNumber() + ")")){
        				mArrayAdapterBL.add(contact.getName() + " (" + contact.getNumber() + ")");
        				addedContacts.add(contact.getName() + " (" + contact.getNumber() + ")");
        				}
        			if (addedContacts.size()==initialSize) 
        				Toast.makeText(getApplicationContext(), "All Contacts are Already in Blacklist!", Toast.LENGTH_LONG).show();
        			}
        		mAutoComplete.setText("");
        		}
        	});

       	mRemoveAll.setOnClickListener(new OnClickListener(){    	 	
       		public void onClick(View v){
        		mArrayAdapterBL.clear();
        		addedContacts.clear();
        		Toast.makeText(getApplicationContext(), "All Contacts Removed from Blacklist!", Toast.LENGTH_LONG).show();
        		mAutoComplete.setText("");
        		}
        	});
        	
       	mRemove.setOnClickListener(new OnClickListener(){    	 	
       		public void onClick(View v){
       			String name = mAutoComplete.getText().toString();
        		if (!addedContacts.contains(name))
        			Toast.makeText(getApplicationContext(), "Contact is not in Blacklist!", Toast.LENGTH_LONG).show();
        		else {
       				mArrayAdapterBL.remove(name);
       				addedContacts.remove(name);
       			}
        		mAutoComplete.setText("");
        		}
        	});

}
    
    private void instantiateVariables(){
        mAddButton = (Button) findViewById(R.id.add);
        mAddAll = (Button) findViewById(R.id.add_all_button);
        mRemoveAll = (Button) findViewById(R.id.remove_all_button);
        mRemove = (Button) findViewById(R.id.remove);
        addedContacts = new ArrayList<String>();
        blacklistedContacts = new LinkedList<String>();
        contacts = new ArrayList<Contact>();
        contactNames = new ArrayList<String>();
        mArrayAdapterBL = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, blacklistedContacts);
        this.setListAdapter(mArrayAdapterBL);
        mArrayAdapterAC = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,contactNames);
        mAutoComplete = (AutoCompleteTextView)findViewById(R.id.blacklist_text);
        mAutoComplete.setAdapter(mArrayAdapterAC);
        mAutoComplete.setThreshold(3); 
    }
    
    public void populatePeopleList() {
    	contacts.clear();
    	contactNames.clear();
    	String phoneNumber = "";
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
	  			while (numbers.moveToNext()) {
	  				phoneNumber = numbers.getString(numbers.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
	  				contacts.add(new Contact(iD, name, phoneNumber));
	  			}
	  		}
    	}
    	people.close();
    	
    	for (Contact contact: contacts){
    		contactNames.add(contact.getName() + " (" + contact.getNumber() + ")");
    	}
    	//startManagingCursor(people);
    }
    
/**	@Override 
 * this was the code for a popupWindow that may be revived one day...
 * but for now it shall live in comments.
 *
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
        
}
