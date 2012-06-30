/**
 * Copyright © 2012 Mike Depinet
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.teamPrime.sm.history.SafeModeOnOffItem;
import com.teamPrime.sm.tasks.BlackListIOTask;
import com.teamPrime.sm.tasks.DateWaitTask;

/**
 * This is our main activity.  It allows the user to start and stop the app, as well as 
 * go to the blacklist management activity.
 * @author Mike Depinet
 * @version 2.0
 */
public class SafeLaunchActivity extends Activity{	
	public static final boolean FULL_VERSION = true;
	
	private boolean applicationOnState = false;
	private boolean privateOnState = false;
	private long offTime;
	private Button onOffButton;
	private Button blacklistButton;
	
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
	
	OnCancelListener dateTimeCancel = new OnCancelListener(){
		@Override
		public void onCancel(DialogInterface arg0) {
			if (mTask != null) mTask.cancel(true);
			try{dismissDialog(TIME_ID);} catch(Exception ex){}
			try{dismissDialog(DATE_ID);} catch(Exception ex){}
			onOffButton.setText(getString(R.string.start_button));
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        onOffButton = (Button)findViewById(R.id.dashboard_onOff);
        blacklistButton = (Button)findViewById(R.id.dashboard_blacklist);

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
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        applicationOnState = data.getBoolean("onState", false);
        offTime = data.getLong("offTime", System.currentTimeMillis());
        handleTimes(true,true);
        seenNotice = data.getBoolean("seenNotice",false);
        boolean writingContacts = data.getBoolean("writingContacts", false);
        data = getSharedPreferences(SafeLaunchActivity.class.getName(), MODE_PRIVATE);
        privateOnState = data.getBoolean("onState", false);
        
        if (writingContacts){
        	BlackListIOTask finIO = new BlackListIOTask(this,null,BlackListIOTask.CONTINUE_CONTACTS_REVEAL);
        	finIO.execute((Void[])(null));
        }
        
        if (!seenNotice) showDialog(NOTICE_ID);
        
        onOffButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(applicationOnState ? R.drawable.dashboard_unlock : R.drawable.dashboard_lock), null, null);
        onOffButton.setText(getString(applicationOnState ? R.string.end_button : R.string.start_button));
        blacklistButton.setText(getString(applicationOnState ? R.string.view_blacklist : R.string.edit_blacklist));
        
        if (applicationOnState){
        	if (privateOnState){ //SafeMode is on and we know it
	        	long timeLeft = 0;
	        	timeLeft = offTime - System.currentTimeMillis();
	        	if (timeLeft <= 0) turnOff(); //Perhaps someone turned off their phone while SafeMode was running
	        	else{
		        	allowTimer = true;
		        	if (timer != null) timer.cancel();
		        	timer = new SimpleTimer(timeLeft,1000);
		            timer.start();
	        	}
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
        	try{
        		d = new TimePickerDialog(this, mTimeSetListener, hour, minute, false);
        	} catch (IllegalArgumentException ex){
        		Calendar c = Calendar.getInstance();
        		d = new TimePickerDialog(this, mTimeSetListener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        	}
        	((TimePickerDialog) d).setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        	d.setOnCancelListener(dateTimeCancel);
            break;
        case DATE_ID:
        	dateUpdated = false;
        	try{
        		d = new DatePickerDialog(this, mDateSetListener, year, month, day);
        	} catch (IllegalArgumentException ex){
        		Calendar c = Calendar.getInstance();
        		d = new DatePickerDialog(this, mDateSetListener, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        	}
        	((DatePickerDialog) d).setButton(DialogInterface.BUTTON_NEGATIVE,
                    getString(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        	d.setOnCancelListener(dateTimeCancel);
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
    	if (mTask != null) mTask.cancel(true);
    	if (ioTask != null) ioTask.cancel(true);
    }
    
    @Override
    public void onBackPressed(){
    	if(isTaskRoot()) moveTaskToBack(!isTaskRoot());
    	else super.onBackPressed();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
            	if (applicationOnState) Toast.makeText(getApplicationContext(), R.string.sett_onState, Toast.LENGTH_SHORT).show();
            	else startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dashboard_onOff:
            	if (!applicationOnState) turnOn();
            	else{
            		int numAttempts = getSharedPreferences("SAFEMODE",MODE_PRIVATE).getInt("failedAttempts", 0);
            		if (numAttempts > MathStopActivity.MAX_ATTEMPTS) Toast.makeText(getApplicationContext(), R.string.stop_attemptsExceeded, Toast.LENGTH_SHORT).show();
            		else{
            			Intent i = new Intent(getApplicationContext(), MathStopActivity.class);
            			startActivity(i);
            		}
            	}
            	break;
            case R.id.dashboard_blacklist:
            	Intent i = new Intent(getApplicationContext(), BlackListActivity.class);
            	i.putExtra("readOnly", applicationOnState);
            	startActivity(i);
            	break;
            case R.id.dashboard_history:
            	if (FULL_VERSION) {
            		startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
            	} else {
            		Toast.makeText(this, getString(R.string.sm_only), Toast.LENGTH_SHORT).show();
            	}
            	break;
            case R.id.dashboard_find_me:
            	if (FULL_VERSION) {
            		startActivity(new Intent(getApplicationContext(), FindMeActivity.class));
            	} else {
            		Toast.makeText(this, getString(R.string.sm_only), Toast.LENGTH_SHORT).show();
            	}
            	break;
        }
    }

   
    
    private void handleTimes(boolean fromLong, boolean updateDate){
    	if (fromLong && updateDate) handleTimes(true,false); //Before we can update the date, our current values need to be set.
    	Calendar c = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
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
    	onOffButton.setText(getString(R.string.waiting_user));
    	mTask = new DateWaitTask(this);
    	mTask.execute((Void[])(null));
    }
    
    public void finishTurnOn(){
    	//Time related variables are now set (by DateWaitTask)
    	if (offTime <= System.currentTimeMillis()){
    		Toast.makeText(getApplicationContext(), getString(R.string.future_reqd), Toast.LENGTH_SHORT).show();
    		onOffButton.setText(getString(R.string.start_button));
    		return;
    	}
		applicationOnState = true;
		privateOnState = true;
        
		onOffButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.dashboard_unlock), null, null);
		onOffButton.setText(getString(R.string.end_button));
		blacklistButton.setText(getString(R.string.view_blacklist));
		
		ioTask = new BlackListIOTask(this,null,BlackListIOTask.HIDE_CONTACTS_MODE);
    	ioTask.execute((Void[])(null));
        
        showIcon();
        
    	long timeLeft = offTime - System.currentTimeMillis();
    	allowTimer = true;
    	if (timer != null) timer.cancel();
    	timer = new SimpleTimer(timeLeft,1000);
        timer.start();
        timeUpdated = dateUpdated = false;
		
		HistoryActivity.addItem(getApplicationContext(), new SafeModeOnOffItem(true, null, null));
    }
    
    private void turnOff(){
    	ioTask = new BlackListIOTask(this,null,BlackListIOTask.REVEAL_CONTACTS_MODE);
    	ioTask.execute((Void[])(null));
    	
    	applicationOnState = false;
    	privateOnState = false;
    	
        allowTimer = false;
        if (timer != null) timer.cancel();
        
    	onOffButton.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.dashboard_lock), null, null);
        onOffButton.setText(getString(R.string.start_button));
        blacklistButton.setText(getString(R.string.edit_blacklist));

        hideIcon();
		
		SharedPreferences.Editor editor = getSharedPreferences("SAFEMODE",MODE_PRIVATE).edit();
		editor.putInt("failedAttempts", 0);
		editor.commit();
		
		HistoryActivity.addItem(getApplicationContext(), new SafeModeOnOffItem(false, null, null));
    }
    
    private void showIcon(){
		Notification not = new Notification(R.drawable.locked, getString(R.string.notif_locked_short), System.currentTimeMillis());
		not.setLatestEventInfo(getApplicationContext(), getString(R.string.notif_locked_message), getString(R.string.notif_click_here),
							  PendingIntent.getActivity(getApplicationContext(), 0, new Intent(getApplicationContext(), MathStopActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK));
		not.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
		NotificationManager notMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notMgr.notify(LockedNotificationId, not);
    }
    private void hideIcon(){
    	NotificationManager notMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notMgr.cancel(LockedNotificationId);
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
			onOffButton.setText(new Date(0).toString().substring(11,19));
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
			onOffButton.setText(dayString+pad(hours)+":"+pad(minutes)+":"+pad(seconds));
		}
		private String pad(int input){
			if (input < 10) return "0"+input;
			else return ""+input;
		}
    }
    
}