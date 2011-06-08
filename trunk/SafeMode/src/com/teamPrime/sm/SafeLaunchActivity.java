package com.teamPrime.sm;

import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
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
	private Menu mMenu;
	
	private TimePickerDialog.OnTimeSetListener mTimeSetListener;
	private DatePickerDialog.OnDateSetListener mDateSetListener;
	private int hour, minute, year, month, day;
	private final int TIME_ID = 0, DATE_ID = 1;
	private CountDownTimer timer;
	
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
        	timer = new SimpleTimer(timeLeft,1000);
            timer.start();
        }
        
        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        	@Override
            public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                hour = view.getCurrentHour();
                minute = view.getCurrentMinute();
            }
        };
        
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int yr, int monthOfYear, int dayOfMonth) {
				year = view.getYear();
				month = view.getMonth();
				day = view.getDayOfMonth();
			}
		};
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	mMenu = menu;
    	String blackString = getString(onState ? R.string.edit_blacklist : R.string.view_blacklist);
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
            return new TimePickerDialog(this, mTimeSetListener, hour, minute, true);
        case DATE_ID:
        	return new DatePickerDialog(this, mDateSetListener, year, month, day);
        }
        return null;
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onOffButton:
            	if (!onState){
            		showDialog(DATE_ID);
            		showDialog(TIME_ID);
            		offTime = (long)((((year*12+month)*30.436875+day)*24+hour)*60+minute)*60000;
            		onState = true;
            		SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
                    SharedPreferences.Editor editor = data.edit();
                    editor.putBoolean("onState", true);
                    editor.putLong("offTime", offTime);
                    editor.commit();
                    
                    onOffButton.setText(getString(R.string.end_button));
                    mMenu.clear();
                	mMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.view_blacklist));
                	long timeLeft = System.currentTimeMillis() - offTime;
                	timer = new SimpleTimer(timeLeft,1000);
                    timer.start();
            	}
            	else turnOff();
        }
    }
    
    private void turnOff(){
    	SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        editor.putBoolean("onState", false);
        editor.putLong("offTime", System.currentTimeMillis());
        editor.commit();
        
        onOffButton.setText(getString(R.string.start_button));
        mMenu.clear();
        mMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.edit_blacklist));
        countdownTimer.setText("");
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
			countdownTimer.setText(new Date(millisLeft).toString().substring(11,19));
		}
    }
    
}