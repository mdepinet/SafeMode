/**
 * Copyright © 2011 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.teamPrime.sm.tasks.BlackListIOTask;
import com.teamPrime.sm.tasks.DateWaitTask;

/**
 * This is our main activity.  It allows the user to start and stop the app, as well as 
 * go to the blacklist management activity.
 * @author Mike Depinet
 * @version 1.0
 */
public class SafeLaunchActivity extends Activity {	
	private boolean applicationOnState = false;
	private boolean privateOnState = false;
	private long offTime;
	private ImageButton onOffButton;
	private TextView onOffText;
	private TextView blacklistText;
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener;
	private DatePickerDialog.OnDateSetListener mDateSetListener;
	private int hour, minute, year, month, day;
	public static final int TIME_ID = 0, DATE_ID = 1, NOTICE_ID = 2;
	private boolean seenNotice = false;
	private CountDownTimer timer;
	private boolean timeUpdated, dateUpdated = false;
	private DateWaitTask mTask;
	private boolean allowTimer = false;
	
	private BlackListIOTask ioTask;
	
	public static final int LockedNotificationId = 1;
	
	//Broadcast Receiver (for notification)
	private BroadcastReceiver mIntentReceiver = null;
	
	//Randomly generated salt and deviceId for licensing stuff
//	private final byte[] SALT = new byte[] {-62,-74,107,-13,110,-84,-29,121,33,-107,-95,7,70,-15,-112,123,20,-21,-62,59};
//	private String deviceId = null;
//	private LicenseChecker mChecker = null;
//	private final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAg8NGOO0Vpkyhq+R/K5iQ4zk7SFpHQqfxUTJ"
//		+"yesFk3ZljkR4dy3U3VyU5Pc8lyOr1Z8ScasRPe37kgAcybd+Mw3UPh9+nZa27DdUZ9U1HwMvCJ0bGl+18hrrjsNNuO/1mUG7XM8IQ1Qm58ngb"
//		+"Jh197mBnvNiNLd+PFeTupvQyPffzciBfGQXflZArUY1pvxrraGDFMB97KdKD17sk+LnYfQ5T9dQnsCPFNEnV0bTyG6tyOXc/KoPOk8O2kVjPg"
//		+"UTKrgq60LOGo8yz8uyUaDxPDvDKoB2pYTdmnq50B/cnc34uOR/NHBc7nYsQs6HC8UYfsYqRQKnsAV/kWkNtkxOrbwIDAQAB";
//	private LicenseCheckerCallback mLicenseCallback = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        onOffButton = (ImageButton)findViewById(R.id.dashboard_onOff);
        onOffText = (TextView)findViewById(R.id.dashboard_onOff_text);
        blacklistText = (TextView)findViewById(R.id.dashboard_blacklist_text);
        
//		if (deviceId == null) deviceId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//		mLicenseCallback = new SafeModeLLC();
//		mChecker = new LicenseChecker(this,
//				new ServerManagedPolicy(this,
//		            new AESObfuscator(SALT, getPackageName(), deviceId)),
//		            PUBLIC_KEY  // Your public licensing key.
//		        );
//		mChecker.checkAccess(mLicenseCallback);

        
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        	@Override
            public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                hour = view.getCurrentHour();
                minute = view.getCurrentMinute();
                timeUpdated = true;
                if (dateUpdated) handleTimes(false,false);
            }
        };
        
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int yr, int monthOfYear, int dayOfMonth) {
				year = view.getYear();
				month = view.getMonth();
				day = view.getDayOfMonth();
				dateUpdated = true;
				if (timeUpdated) handleTimes(false,false);
			}
		};
		
		mIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
		        boolean onState = data.getBoolean("onState", false);
				if(onState) {
					int icon = R.drawable.locked;
					CharSequence tickerText = getString(R.string.notif_locked_short);
					long when = System.currentTimeMillis();
					
					Notification not = new Notification(icon, tickerText, when);
					not.setLatestEventInfo(context, 
										  getString(R.string.notif_locked_message), 
										  getString(R.string.notif_click_here), 
										  PendingIntent.getActivity(context, 0, new Intent(getApplicationContext(), MathStopActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK)
										  );
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
					mNotificationManager.notify(LockedNotificationId, not);
				}
			}
		};
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        applicationOnState = data.getBoolean("onState", false);
        offTime = data.getLong("offTime", System.currentTimeMillis());
        handleTimes(true,true);
        seenNotice = data.getBoolean("seenNotice",false);
        data = getSharedPreferences(SafeLaunchActivity.class.getName(), MODE_PRIVATE);
        privateOnState = data.getBoolean("onState", false);
        
        if (!seenNotice) showDialog(NOTICE_ID);
        
        onOffButton.setImageDrawable(getResources().getDrawable(applicationOnState ? R.drawable.turn_off_button : R.drawable.turn_on_button));
        onOffText.setText(getString(applicationOnState ? R.string.end_button : R.string.start_button));
        blacklistText.setText(getString(applicationOnState ? R.string.view_blacklist : R.string.edit_blacklist));
        
        if (applicationOnState){
        	if (privateOnState){
	        	long timeLeft = 0;
	        	timeLeft = offTime - System.currentTimeMillis();
	        	allowTimer = true;
	        	if (timer != null) timer.cancel();
	        	timer = new SimpleTimer(timeLeft,1000);
	            timer.start();
        	}
        	else{ //If the application was started from a different activity
        		turnOn();
        	}
        }
        else{
        	if (!privateOnState){
	        	//If we already knew we were off, we shouldn't have to do anything
        	}
        	else{
        		turnOff(); //If someone else turned us off, make sure we're actually off
        	}
        }
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean("onState", applicationOnState);
        editor.putLong("offTime", offTime);
        editor.commit();
        data = getSharedPreferences(SafeLaunchActivity.class.getName(), MODE_PRIVATE);
        editor = data.edit();
        editor.putBoolean("onState", privateOnState);
        editor.commit();
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	Dialog d = null;
        switch (id) {
        case TIME_ID:
        	timeUpdated = false;
            d = new TimePickerDialog(this, mTimeSetListener, hour, minute, false);
            break;
        case DATE_ID:
        	dateUpdated = false;
        	d = new DatePickerDialog(this, mDateSetListener, year, month, day);
        	break;
        case NOTICE_ID:
        	d = new AlertDialog.Builder(this).setMessage(getString(R.string.init_notice)).setCancelable(false)
        		.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        	        	   SharedPreferences.Editor editor = data.edit();
        	        	   editor.putBoolean("seenNotice", true);
        	        	   editor.commit();
        	        	   dialog.dismiss();
        	           }
        		}).create();
        	break;
        }
        return d;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
//    	mChecker.onDestroy();
    	if (mTask != null) mTask.cancel(true);
    	if (ioTask != null) ioTask.cancel(true);
    }
    
    @Override
    public void onBackPressed(){
    	if(isTaskRoot()) moveTaskToBack(!isTaskRoot());
    	else super.onBackPressed();
    }
    
    
    //TODO
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dashboard_onOff:
            	if (!applicationOnState) turnOn();
            	else{
            		startActivity(new Intent(getApplicationContext(), MathStopActivity.class));
            	}
            	break;
            case R.id.dashboard_blacklist:
            	Intent i = new Intent(getApplicationContext(), BlackListActivity.class);
            	i.putExtra("readOnly", applicationOnState);
            	startActivity(i);
            	break;
            case R.id.dashboard_history:
            	startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            	break;
            case R.id.dashboard_find_me:
            	//TODO
            	break;
        }
    }
    
    private void handleTimes(boolean fromLong, boolean updateDate){
    	if (fromLong && updateDate) handleTimes(true,false); //Before we can update the date, our current values need to be set.
    	Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
    	Log.v("SAFEMODE - times", "Curr: "+System.currentTimeMillis()+". Off: "+offTime);
    	if (updateDate){
    		int currYear, currMonth, currDay;
    		currYear = c.get(Calendar.YEAR);
    		currMonth = c.get(Calendar.MONTH);
    		currDay = c.get(Calendar.DAY_OF_MONTH);
    		if (currDay+30.436875*currMonth+365.2425*currYear > day+30.436875*month+365.2425*year){ //If offTime has past date
    			year = currYear;
    			month = currMonth;
    			day = currDay;
    		}
    	}
    	if (fromLong){
    		c.setTimeInMillis(offTime);
    		hour = c.get(Calendar.HOUR);
    		minute = c.get(Calendar.MINUTE);
    		if (!updateDate){
    			year = c.get(Calendar.YEAR);
    			month = c.get(Calendar.MONTH);
    			day = c.get(Calendar.DAY_OF_MONTH);
    		}
    	}
    	else{
    		c.set(year, month, day, hour, minute);
    		offTime = c.getTimeInMillis();
    	}
    }
    
    private void turnOn(){
    	onOffText.setText(getString(R.string.waiting_user));
    	mTask = new DateWaitTask(this);
    	mTask.execute((Void[])(null));
    }
    
    public void finishTurnOn(){
    	//Time related variables are now set (by DateWaitTask)
    	if (offTime <= System.currentTimeMillis()){
    		Toast.makeText(getApplicationContext(), getString(R.string.future_reqd), Toast.LENGTH_SHORT).show();
    		onOffText.setText(getString(R.string.start_button));
    		return;
    	}
    	ioTask = new BlackListIOTask(this,null,BlackListIOTask.HIDE_CONTACTS_MODE);
    	ioTask.execute((Void[])(null));
    	
		applicationOnState = true;
		privateOnState = true;
        
		onOffButton.setImageDrawable(getResources().getDrawable(R.drawable.turn_off_button));
		onOffText.setText(getString(R.string.end_button));
		blacklistText.setText(getString(R.string.view_blacklist));
        
        // Register the Receiver to call the Unlock Page when phone unlocks
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
    	registerReceiver(mIntentReceiver, mIntentFilter);
		Log.v("SAFEMODE - Receiver", "Finished registration of broadcast receiver");
        
    	long timeLeft = offTime - System.currentTimeMillis();
    	allowTimer = true;
    	if (timer != null) timer.cancel();
    	timer = new SimpleTimer(timeLeft,1000);
        timer.start();
        timeUpdated = dateUpdated = false;
        
		int icon = R.drawable.locked;
		CharSequence tickerText = getString(R.string.notif_locked_short);
		long when = System.currentTimeMillis();
		Notification not = new Notification(icon, tickerText, when);
		not.setLatestEventInfo(getApplicationContext(), 
							  getString(R.string.notif_locked_message), 
							  getString(R.string.notif_click_here), 
							  PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MathStopActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK)
							  );
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.notify(LockedNotificationId, not);
    }
    
    private void turnOff(){
    	ioTask = new BlackListIOTask(this,null,BlackListIOTask.REVEAL_CONTACTS_MODE);
    	ioTask.execute((Void[])(null));
    	
    	applicationOnState = false;
    	privateOnState = false;
    	
        allowTimer = false;
        if (timer != null) timer.cancel();
        
    	onOffButton.setImageDrawable(getResources().getDrawable(R.drawable.turn_on_button));
        onOffText.setText(getString(R.string.start_button));
        blacklistText.setText(getString(R.string.edit_blacklist));

		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancel(LockedNotificationId);
    }
    
    //Getters and Setters for time related variables
    public int getHour() {return hour;}
	public int getMinute() {return minute;}
	public int getYear() {return year;}
	public int getMonth() {return month;}
	public int getDay() {return day;}
	
	public boolean isTimeUpdated() {return timeUpdated;}
	public boolean isDateUpdated() {return dateUpdated;}

	public void setHour(int hour) {this.hour = hour;}
	public void setMinute(int minute) {this.minute = minute;}
	public void setYear(int year) {this.year = year;}
	public void setMonth(int month) {this.month = month;}
	public void setDay(int day) {this.day = day;}

	public void setTimeUpdated(boolean timeUpdated) {this.timeUpdated = timeUpdated;}
	public void setDateUpdated(boolean dateUpdated) {this.dateUpdated = dateUpdated;}

	
	class SimpleTimer extends CountDownTimer{
    	public SimpleTimer(long millisInFuture, long countDownInterval){
    		super(millisInFuture, countDownInterval);
    	}
		@Override
		public void onFinish() {
			onOffText.setText(new Date(0).toString().substring(11,19));
			turnOff();
		}
		@Override
		public void onTick(long millisLeft) {
			if (!allowTimer) return;
			int days = (int) (millisLeft/86400000);
			millisLeft %= 86400000;
			int hours = (int) (millisLeft/3600000);
			millisLeft %= 3600000;
			int minutes = (int) (millisLeft/60000);
			millisLeft %= 60000;
			int seconds = (int) (millisLeft/1000);
			String dayString = days == 0 ? "" : ""+days+"days ";
			onOffText.setText(dayString+pad(hours)+":"+pad(minutes)+":"+pad(seconds));
		}
		private String pad(int input){
			if (input < 10) return "0"+input;
			else return ""+input;
		}
    }
	
//	private class SafeModeLLC implements LicenseCheckerCallback{
//		@Override
//		public void allow() {
//			//Proceed as usual
//			Log.i("SAFEMODE","Licensing success");
//		}
//		@Override
//		public void dontAllow() {
//			Toast.makeText(getApplicationContext(), "You don't have a license. Sorry, bro", Toast.LENGTH_SHORT).show();
//			finish();
//		}
//		@Override
//		public void applicationError(ApplicationErrorCode errorCode) {
//			Toast.makeText(getApplicationContext(), "Failed to load license.\nServer returned: "+errorCode, Toast.LENGTH_SHORT).show();
//			finish();
//		}
//	}
    
}