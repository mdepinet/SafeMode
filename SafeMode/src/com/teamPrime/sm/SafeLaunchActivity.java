package com.teamPrime.sm;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * This is our main activity.  It allows the user to start and stop the app, as well as 
 * go to the blacklist management activity.
 * @author Mike Depinet
 * @version 1.0
 */
public class SafeLaunchActivity extends Activity {	
	private boolean onState;
	private long offTime;
	private Button onOffButton;
	private TextView countdownTimer;
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener;
	private DatePickerDialog.OnDateSetListener mDateSetListener;
	private int hour, minute, year, month, day;
	public static final int TIME_ID = 0, DATE_ID = 1;
	private CountDownTimer timer;
	private boolean timeUpdated, dateUpdated = false;
	private DateWaitTask mTask;
	private boolean allowTimer = false;
	
	private Menu mMenu;
	private final String edit_blacklist = "Edit Blacklist";
	private final String view_blacklist = "View Blacklist";
	
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			int icon = R.drawable.icon;
			CharSequence tickerText = "SAFEMODE Locked";
			long when = System.currentTimeMillis();
			
			Notification not = new Notification(icon, tickerText, when);
			not.setLatestEventInfo(context, 
								  "Your contacts have been protected by SAFEMODE", 
								  "Click here to unlock SAFEMODE", 
								  PendingIntent.getActivity(context, 0, new Intent(getApplicationContext(), UnlockPhoneActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK)
								  );
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			
			mNotificationManager.notify(1, not);
			
		}
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        onState = data.getBoolean("onState", false);
        offTime = data.getLong("offTime", System.currentTimeMillis());

        onOffButton = (Button)findViewById(R.id.onOffButton);
        countdownTimer = (TextView)findViewById(R.id.countdownTimer);
        
        onOffButton.setText(getString(onState ? R.string.end_button : R.string.start_button));
        
        long timeLeft = 0;
        if (onState){
        	timeLeft = System.currentTimeMillis() - offTime;
        	allowTimer = true;
        	if (timer != null) timer.cancel();
        	timer = new SimpleTimer(timeLeft,1000);
            timer.start();
        }
        
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        	@Override
            public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                hour = view.getCurrentHour();
                minute = view.getCurrentMinute();
                timeUpdated = true;
            }
        };
        
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int yr, int monthOfYear, int dayOfMonth) {
				year = view.getYear();
				month = view.getMonth();
				day = view.getDayOfMonth();
				dateUpdated = true;
			}
		};
		Calendar c = Calendar.getInstance(TimeZone.getDefault(),Locale.getDefault());
		c.setTimeInMillis(offTime);
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		c = Calendar.getInstance(TimeZone.getDefault(),Locale.getDefault());
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
		if (year == 0 && month == 0 && day == 0 && hour == 0 && minute == 0){
			hour = c.get(Calendar.HOUR_OF_DAY);
			minute = c.get(Calendar.MINUTE);
		}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        onState = data.getBoolean("onState", false);
        offTime = data.getLong("offTime", System.currentTimeMillis());
        
        onOffButton.setText(getString(onState ? R.string.end_button : R.string.start_button));
        
        long timeLeft = 0;
        if (onState){
        	timeLeft = offTime - System.currentTimeMillis();
        	allowTimer = true;
        	if (timer != null) timer.cancel();
        	timer = new SimpleTimer(timeLeft,1000);
            timer.start();
        }
        else{
        	allowTimer = false;
        	countdownTimer.setText("");
        	if (timer != null) timer.cancel();
        }
        
        if (mMenu != null){
        	mMenu.clear();
        	String blackString = onState ? view_blacklist : edit_blacklist;
        	mMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, blackString);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	mMenu = menu;
//    	String blackString = getText(R.string.edit_blacklist).toString();
//    	String blackString = getString(onState ? R.string.edit_blacklist : R.string.view_blacklist);
    	String blackString = onState ? view_blacklist : edit_blacklist;
    	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, blackString);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i = new Intent(getApplicationContext(), BlackListActivity.class);
    	i.putExtra("readOnly", onState);
    	startActivity(i);
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case TIME_ID:
        	timeUpdated = false;
            return new TimePickerDialog(this, mTimeSetListener, hour, minute, false);
        case DATE_ID:
        	dateUpdated = false;
        	return new DatePickerDialog(this, mDateSetListener, year, month, day);
        }
        return null;
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	mTask.cancel(true);
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOffButton:
            	if (!onState) turnOn();
            	else{
            		//turnOff();
            		startActivity(new Intent(getApplicationContext(), MathStopActivity.class));
            	}
        }
    }
    
    private void turnOn(){
    	countdownTimer.setText("Waiting for user input...");
    	mTask = new DateWaitTask(this);
    	mTask.execute((Void[])(null));
    	
    	// Make notification here
        // Register the Receiver to call the Unlock Page when phone unlocks
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(Intent.ACTION_USER_PRESENT);
    	registerReceiver(mIntentReceiver, mIntentFilter);
		Log.v("registeredReceiver", "finishedregistration");
    }
    public void finishTurnOn(){
    	Calendar c = Calendar.getInstance(TimeZone.getDefault(),Locale.getDefault());
//		c.add(Calendar.YEAR, year-1900);
//		c.add(Calendar.MONTH, month);
//		c.add(Calendar.DAY_OF_MONTH, day);
//		c.add(Calendar.HOUR_OF_DAY, hour);
//		c.add(Calendar.MINUTE, minute);
    	c.set(year, month, day, hour, minute);
		
		//offTime = (long)((((year*12+month)*30.436875+day)*24+hour)*60+minute)*60000;
		offTime = c.getTimeInMillis();
		onState = true;
		SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean("onState", true);
        editor.putLong("offTime", offTime);
        editor.commit();
        
        onOffButton.setText(getString(R.string.end_button));
        if (mMenu != null){
            mMenu.clear();
            mMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, view_blacklist);
        }
    	long timeLeft = offTime - System.currentTimeMillis();
    	allowTimer = true;
    	if (timer != null) timer.cancel();
    	timer = new SimpleTimer(timeLeft,1000);
        timer.start();
        timeUpdated = dateUpdated = false;
    }
    
    private void turnOff(){
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        onState = false;
        editor.putBoolean("onState", false);
        editor.commit();
        
        onOffButton.setText(getString(R.string.start_button));
        allowTimer = false;
        if (timer != null) timer.cancel();
        countdownTimer.setText("");
        
        if (mMenu != null){
	        mMenu.clear();
	        mMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, edit_blacklist);
        }
    }
    
    public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public boolean isTimeUpdated() {
		return timeUpdated;
	}
	
	public boolean isDateUpdated() {
		return dateUpdated;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setTimeUpdated(boolean timeUpdated) {
		this.timeUpdated = timeUpdated;
	}
	
	public void setDateUpdated(boolean dateUpdated) {
		this.dateUpdated = dateUpdated;
	}

	class SimpleTimer extends CountDownTimer{
    	public SimpleTimer(long millisInFuture, long countDownInterval){
    		super(millisInFuture, countDownInterval);
    	}

		@Override
		public void onFinish() {
			countdownTimer.setText(new Date(0).toString().substring(11,19));
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
			//countdownTimer.setText(dayString+(new Date(millisLeft).toString().substring(11,19)));
			countdownTimer.setText(dayString+pad(hours)+":"+pad(minutes)+":"+pad(seconds));
		}
		private String pad(int input){
			if (input < 10) return "0"+input;
			else return ""+input;
		}
    }
    
}