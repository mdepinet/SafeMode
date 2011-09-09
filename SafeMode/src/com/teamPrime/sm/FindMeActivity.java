/**
 * Copyright © 2011 Boris Treskunov
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.teamPrime.sm.history.FindMeItem;
import com.teamPrime.sm.history.HistAction;
import com.teamPrime.sm.history.HistoryItem;
import com.teamPrime.sm.history.action.ResendTextAction;
import com.teamPrime.sm.history.action.ViewTextAction;
import com.teamPrime.sm.tasks.PopulateTaskFindMe;

public class FindMeActivity extends ListActivity {
	private static final String SHARED_PREF_NAME = "SafeMode - FindMe";
	
	private ProgressDialog loading;
	private EditText mEditText;
	
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location currentLocation;
	private List<Address> currentAddressList;
	private Address currentAddress;
	private String phoneNumber;
	private String currentName;
	private String enteredText;
	private double currentLat;
	private double currentLong;
	private boolean locationFound;
	
	private List<Long> contactIds = new LinkedList<Long>();
	private Map<String, Long> nameToIdMap = new TreeMap<String, Long>();
	private List<String> contacts = new LinkedList<String>();
	private List<String> addedContacts = new LinkedList<String>();
	private ArrayAdapter<String> mArrayAdapter;
	private PopulateTaskFindMe mTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_me);
		
		locationManager		= 	(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationFound		=	false;
		mEditText			=	(EditText) findViewById(R.id.findme_contact_text);
		getListView().setTextFilterEnabled(true);
		mEditText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) { 
				enteredText = s.toString();
				mArrayAdapter.getFilter().filter(enteredText);
				Log.e("FindMeActivity", "afterTextChanged = " + s.toString() + ", enteredText = " + enteredText);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) { }

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) { }
		});
		
		instantiateList();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor phones = getContentResolver().query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ nameToIdMap.get(mArrayAdapter.getItem(position)),
				null, null);
				while (phones.moveToNext()) {

				//find number associated with chosen contact
				currentName = mArrayAdapter.getItem(position);
				phoneNumber = phones.getString(
				phones.getColumnIndex(
				ContactsContract.CommonDataKinds.Phone.NUMBER));}
				phones.close();
				
		getLocationAndSendSMS(); 
	}
	
	public void getLocationAndSendSMS(){
		locationFound = false;
		loading = ProgressDialog.show(this, "", getString(R.string.loading_location), true);
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.e("FindMeActivity", "onLocationChanged called, location = " + location);
		    	loading.dismiss();
		    	locationFound = true;
		    	currentLocation = location;
		    	locationManager.removeUpdates(locationListener);
		    	try {
					updateAddress();
				} 
				catch (IOException e) {
					// TODO Auto-generated catch block
					Log.e("FindMeActivity", e.getMessage());
				}
				initiateSMS();
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };
		  
		  //GPS_Provider or NETWORK_PROVIDER?
		  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		  
		  //check if location update is taking too long, 20 seconds?
	    new CountDownTimer(20000, 50000){
			@Override
			public void onFinish() {
				if(!locationFound){
					loading.dismiss();
					Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					Log.e("FindMeActivity", "lastKnownLocation = " + lastKnownLocation);
					//if last know location is available
					if(lastKnownLocation != null){
						currentLocation = lastKnownLocation;
						try {
							updateAddress();
						} 
						catch (IOException e) {
							// TODO Auto-generated catch block
							Log.e("FindMeActivity", e.getMessage());
						}
					}
					else
						Toast.makeText(getApplicationContext(), "Unable to determine location", Toast.LENGTH_LONG).show();
					locationManager.removeUpdates(locationListener);
					initiateSMS();
				}
			}
			@Override
			public void onTick(long millisUntilFinished) {}			  
		  }.start();

	}
	
	public void updateAddress() throws IOException{
		if (currentLocation != null){
			currentLat = currentLocation.getLatitude();
			currentLong = currentLocation.getLongitude();
			Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
			currentAddressList = gc.getFromLocation(currentLat, currentLong, 1);
			currentAddress = currentAddressList.get(0);
		}
	}
	
	public void initiateSMS(){
		try{
			if (phoneNumber == null)
				phoneNumber = "";
			String address = "an undetermined location";
			if (currentAddress != null)
				address = currentAddress.getAddressLine(0) + "\n" + currentAddress.getAddressLine(1);
			String message = "I am at " + address;
			Log.e("FindMeActivity", "about to send sms, currentAddress = " + currentAddress);
			sendSMS(phoneNumber, message);
		}
		catch(Exception e){
			Log.e("message send error", e.getMessage());
			//Toast.makeText(getApplicationContext(), "I'm sorry, tablets do not support texting", Toast.LENGTH_SHORT).show();
		}
	}

	 private void sendSMS(final String phoneNumber, final String message) {        
	    	String SENT = "SMS_SENT";
	        String DELIVERED = "SMS_DELIVERED";
	 
	        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(SENT), 0);
	 
	        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
	            new Intent(DELIVERED), 0);
	 
	        //---when the SMS has been sent---
	        registerReceiver(new BroadcastReceiver(){
	            @Override
	            public void onReceive(Context arg0, Intent arg1) {
	                switch (getResultCode())
	                {
	                    case Activity.RESULT_OK:
	                        if (currentName == null)
	                        	currentName = "undetermined contact";
	                    	Toast.makeText(getBaseContext(), "Location sent to " + currentName, 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
	                        Toast.makeText(getBaseContext(), "An error occured, please try again", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_NO_SERVICE:
	                        Toast.makeText(getBaseContext(), "No service", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                    case SmsManager.RESULT_ERROR_RADIO_OFF:
	                        Toast.makeText(getBaseContext(), "Radio off, unable to send message", 
	                                Toast.LENGTH_SHORT).show();
	                        break;
	                }
	                //Add to history
	                HistAction viewText = new ViewTextAction(phoneNumber, message, getResultCode());
	                HistAction resendText = new ResendTextAction(phoneNumber, message, true);
	                HistoryItem item = new FindMeItem(null, phoneNumber, currentName, viewText, resendText);
	                HistoryActivity.addItem(getBaseContext(), item);
	            }
	        }, new IntentFilter(SENT));    
	 
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
	    }
	 
		public void populatePeopleList() {
			contactIds.clear();
			contacts.clear();
	    	Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
	    			new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER},
	    			ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1 AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + " = 1",
	    			null, ContactsContract.Contacts.DISPLAY_NAME + " DESC");
	    
	    	while (people.moveToNext()){
		    	String name = people.getString(0);
		    	Long id = people.getLong(1);
		    	contacts.add(name);
		    	contactIds.add(id);
		    	nameToIdMap.put(name, id);
	    	}
	    	
	    	people.close();
	    }
		
		public void instantiateList(){
        	contacts = new ArrayList<String>();
            mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addedContacts);
            this.setListAdapter(mArrayAdapter);
            mTask = new PopulateTaskFindMe(this);
        	mTask.execute((Void[])(null));
    }    

	public void populateList(){
		Collections.sort(contacts);
		for (String name : contacts)
			mArrayAdapter.add(name);
	}

    public void setArrayAdapter(ArrayAdapter<String> mArrayAdapter) {
		this.mArrayAdapter = mArrayAdapter;
	}

	public ArrayAdapter<String> getArrayAdapter() {
		return mArrayAdapter;
	}
	
	public List<String> getContactNames(){
		return addedContacts;
	}
	 
}


