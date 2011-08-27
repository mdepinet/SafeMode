/**
 * Copyright © 2011 Boris Treskunov
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.util.Random;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MathStopActivity extends Activity{
	public static final int MAX_ATTEMPTS = 3;
	
	private TextView question;
	private TextView answer;
	private int corAnswer;
	private boolean onState;
	private int numAttempts;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_stop);
 
        question = (TextView) findViewById(R.id.questionStopText);
        answer = (TextView) findViewById(R.id.answerScreenStop);
        SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        onState = data.getBoolean("onState", true);
        numAttempts = data.getInt("failedAttempts", 0);
        if (!onState) finish(); //Don't even run if SAFEMODE is off.  This should return the user to the main screen
        if (numAttempts >= MAX_ATTEMPTS){
        	Toast.makeText(getBaseContext(), "Too many failed attempts!", Toast.LENGTH_SHORT).show();
        	finish();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    /** Called upon subsequent calls to Activity **/
    
    @Override
   protected void onResume() {
    	super.onResume();
        int mult1 = MathUtils.genSmallRandom();
        int mult2 = MathUtils.genLargeRandom();
        String mathQuestion = String.format(getString(R.string.math_question)+" %d * %d?", mult1, mult2);
    	question.setText(mathQuestion);
    	corAnswer = mult1 * mult2;
    }
    
    public void onClick(View v) {
    	if (v.getId() == R.id.submitStopButton) {
    		String drunkAnsString = answer.getText().toString();
    		try {
    			int drunkAns = Integer.parseInt(drunkAnsString);
    			if (drunkAns == corAnswer) {
					SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
					SharedPreferences.Editor editor = data.edit();
					editor.putBoolean("onState", false);
					onState = false;
					editor.commit();
					
					// get rid of notifications
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
					mNotificationManager.cancel(SafeLaunchActivity.LockedNotificationId);
					
					Toast.makeText(getApplicationContext(), getString(R.string.turn_off), Toast.LENGTH_SHORT).show();
					finish();
    			}
    			else {
    				SharedPreferences.Editor editor = getSharedPreferences("SAFEMODE", MODE_PRIVATE).edit();
    				editor.putInt("failedAttempts", ++numAttempts);
    				editor.commit();
    				Toast.makeText(getApplicationContext(), getString(R.string.wrong_ans) + "\n"+(MAX_ATTEMPTS-numAttempts)+" attempt"+(MAX_ATTEMPTS-numAttempts != 1 ? "s" : "")+" remaining", Toast.LENGTH_SHORT).show();
    				finish();
    			}
    		}
    		catch (NumberFormatException e) {
    			SharedPreferences.Editor editor = getSharedPreferences("SAFEMODE", MODE_PRIVATE).edit();
				editor.putInt("failedAttempts", ++numAttempts);
				editor.commit();
    			Toast.makeText(getApplicationContext(), getString(R.string.wrong_ans) + "\n"+(MAX_ATTEMPTS-numAttempts)+" attempt"+(MAX_ATTEMPTS-numAttempts != 1 ? "s" : "")+" remaining", Toast.LENGTH_SHORT).show();
    			finish();
    			
    		}
    		
    	}
    }
    
    
    static class MathUtils {
    	
    	private static final int[] smallChoice = {3, 4, 6, 7, 8, 9};
    	private static final int[] largeChoice = {12, 13, 14, 16, 17, 18, 19};
    	private static Random r = new Random();
    	
    	public static int genSmallRandom() {
    		int index = r.nextInt(smallChoice.length);
    		return smallChoice[index];
    	}
    	
    	public static int genLargeRandom() {
    		int index = r.nextInt(largeChoice.length);
    		return largeChoice[index];
    	}

    }
}
