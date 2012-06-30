/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.teamPrime.sm.util.MathUtils;
import com.teamPrime.sm.util.MathUtils.MathUtilException;
import com.teamPrime.sm.util.Solvable;


public class MathStopActivity extends Activity{
	public static final int MAX_ATTEMPTS = 3;
	
	private TextView question;
	private TextView answer;
	private long corAnswer;
	private boolean onState;
	private int numAttempts;

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
        	Toast.makeText(getBaseContext(), getString(R.string.stop_attemptsExceeded), Toast.LENGTH_SHORT).show();
        	finish();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    
    @Override
   protected void onResume() {
    	super.onResume();
    	SharedPreferences data = getSharedPreferences("SAFEMODE_Settings", MODE_PRIVATE);
    	List<MathUtils.Operator> operators = new LinkedList<MathUtils.Operator>();
    	if (data.getBoolean("includePlus", true)) operators.add(MathUtils.Operator.ADDITION);
    	if (data.getBoolean("includeMinus", true)) operators.add(MathUtils.Operator.SUBTRACTION);
    	if (data.getBoolean("includeTimes", true)) operators.add(MathUtils.Operator.MULTIPLICATION);
        Solvable equation = MathUtils.generateProblem(operators, data.getInt("numDigits", 2), data.getInt("exprLength", 2));
    	boolean gotValidEquation = false;
    	while (!gotValidEquation){
	        try {
				question.setText(getString(R.string.math_question) + " " + equation.getHumanReadableEquation() + "?");
				corAnswer = equation.getCorrectAnswer();
				gotValidEquation = true;
			} catch (MathUtilException e) {
				Log.e("SafeMode - eqnGen","Created invalid equation",e);
			}
    	}
    }
    
    public void onClick(View v) {
    	if (v.getId() == R.id.submitStopButton) {
    		String drunkAnsString = answer.getText().toString();
    		try {
    			long drunkAns = Long.parseLong(drunkAnsString);
    			if (drunkAns == corAnswer) {
					SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
					SharedPreferences.Editor editor = data.edit();
					editor.putBoolean("onState", false);
					onState = false;
					editor.commit();
					
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
}
