/**
 * Copyright © 2011 Boris Treskunov
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sml;

import java.util.Random;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.teamPrime.sml.R;


public class MathStopActivity extends Activity{
	
	private TextView question;
	private TextView answer;
	private int corAnswer;
	private boolean onState;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_stop);
 
        question = (TextView) findViewById(R.id.questionStopText);
        answer = (TextView) findViewById(R.id.answerScreenStop);
        SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        onState = data.getBoolean("onState", true);
        if (!onState) finish(); //Don't even run if SAFEMODE is off.  This should return the user to the main screen
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
					Intent i = new Intent(getApplicationContext(), SafeLaunchActivity.class);
			    	startActivity(i);
					finish();
    			}
    			else {
    				Toast.makeText(getApplicationContext(), getString(R.string.wrong_ans), Toast.LENGTH_SHORT).show();
    				finish();
    			}
    		}
    		catch (NumberFormatException e) {
    			Toast.makeText(getApplicationContext(), getString(R.string.wrong_ans), Toast.LENGTH_SHORT).show();
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
