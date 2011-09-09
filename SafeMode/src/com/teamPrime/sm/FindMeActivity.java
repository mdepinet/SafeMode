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
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.teamPrime.sm.history.FindMeItem;
import com.teamPrime.sm.history.HistAction;
import com.teamPrime.sm.history.HistoryItem;
import com.teamPrime.sm.history.action.ResendTextAction;
import com.teamPrime.sm.history.action.ViewTextAction;

public class FindMeActivity extends ListActivity {
	private static final int STARTING_CALLBACK_DIALOG_ID = -1;
	private static final String SHARED_PREF_NAME = "SafeMode - FindMe";
	
	private Button findMeButton;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private Location currentLocation;
	private List<Address> currentAddressList;
	private Address currentAddress;
	private double currentLat;
	private double currentLong;
	private boolean locationFound;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.find_me);
		
		findMeButton		=	(Button) findViewById(R.id.findme_button);
		locationManager		= 	(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationFound		=	false;
	}

	public void onClick(View v){
		switch (v.getId()) {
			case R.id.findme_button:
				getLocation();
		}
	}
	
	public void getLocation(){
		locationFound = false;
		locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		    	Log.e("FindMeActivity", "onLocationChanged called, location = " + location);
		    	//Toast.makeText(getApplicationContext(), "location = " + location.toString(), Toast.LENGTH_LONG).show();
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
		  
		  //check if location update is taking too long, 24 seconds?
	    new CountDownTimer(24000, 50000){
			@Override
			public void onFinish() {
				if(!locationFound){
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
			//sends message but does not add it to sent messages:
			//String phoneNumber = "3057789281"; //temporary number
			String phoneNumber = "8329717948";
			String address = "undetermined location";
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
	                        Toast.makeText(getBaseContext(), "Location sent to " + "contact name...", 
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
	                HistoryItem item = new FindMeItem(null, phoneNumber, viewText, resendText);
	                HistoryActivity.addItem(getBaseContext(), item);
	            }
	        }, new IntentFilter(SENT));    
	 
	        SmsManager sms = SmsManager.getDefault();
	        sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);        
	    }
	 
}


