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
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.teamPrime.sm.util.MathUtils;
import com.teamPrime.sm.util.MathUtils.MathUtilException;
import com.teamPrime.sm.util.Solvable;


public class SettingsActivity extends Activity{
	private CheckBox plusBox;
	private CheckBox minusBox;
	private CheckBox timesBox;
	private SeekBar numDigitsBar;
	private SeekBar exprLengthBar;
	private TextView example;
	private TextView numDigitsDisplay;
	
	private boolean includePlus;
	private boolean includeMinus;
	private boolean includeTimes;
	private int numDigits;
	private int exprLength;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
 
        plusBox = (CheckBox) findViewById(R.id.sett_addition_check);
        minusBox = (CheckBox) findViewById(R.id.sett_subtraction_check);
        timesBox = (CheckBox) findViewById(R.id.sett_multiplication_check);
        numDigitsBar = (SeekBar) findViewById(R.id.sett_num_digits);
        exprLengthBar = (SeekBar) findViewById(R.id.sett_expr_length);
        example = (TextView) findViewById(R.id.sett_example);
        numDigitsDisplay = (TextView) findViewById(R.id.sett_num_digits_disp);
        
        plusBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				includePlus = plusBox.isChecked();
				setEnabledChecks();
				generateExample();
			}
        });
        minusBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				includeMinus = minusBox.isChecked();
				setEnabledChecks();
				generateExample();
			}
        });
        timesBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				includeTimes = timesBox.isChecked();
				setEnabledChecks();
				generateExample();
			}
        });
        numDigitsBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				numDigits = numDigitsBar.getProgress()+1; //Progress bar goes to 0, but our minimum is 1
				numDigitsDisplay.setText(""+numDigits);
				generateExample();
			}
        });
        exprLengthBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
			@Override
			public void onStartTrackingTouch(SeekBar arg0) {}
			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				exprLength = exprLengthBar.getProgress()+2; //Progress bar goes to 0, but our minimum is 2
				generateExample();
			}
        });
    }

    
    @Override
   protected void onResume() {
    	super.onResume();
    	SharedPreferences data = getSharedPreferences("SAFEMODE_Settings", MODE_PRIVATE);
        includePlus = data.getBoolean("includePlus", true);
        includeMinus = data.getBoolean("includeMinus", true);
        includeTimes = data.getBoolean("includeTimes", true);
        numDigits = data.getInt("numDigits", 3);
        exprLength = data.getInt("exprLength", 2);
        boolean onState = getSharedPreferences("SAFEMODE", MODE_PRIVATE).getBoolean("onState", false);
        if (onState) finish(); //Don't even run if SAFEMODE is on.  This should return the user to the main screen
        
        plusBox.setChecked(includePlus);
        minusBox.setChecked(includeMinus);
        timesBox.setChecked(includeTimes);
        numDigitsBar.setProgress(numDigits-1); //Progress bar goes to 0, but our minimum is 1
        exprLengthBar.setProgress(exprLength-2); //Progress bar goes to 0, but our minimum is 2
        numDigitsDisplay.setText(""+numDigits);
        
        generateExample();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	SharedPreferences.Editor edit = getSharedPreferences("SAFEMODE_Settings", MODE_PRIVATE).edit();
    	edit.putBoolean("includePlus", includePlus);
    	edit.putBoolean("includeMinus", includeMinus);
    	edit.putBoolean("includeTimes", includeTimes);
    	edit.putInt("numDigits", numDigits);
    	edit.putInt("exprLength", exprLength);
    	edit.commit();
    }
    
    private void setEnabledChecks(){
    	if (!includeMinus && !includeTimes){
    		assert(includePlus);
    		plusBox.setClickable(false);
    	} else if (!includePlus && !includeTimes){
    		assert(includeMinus);
    		minusBox.setClickable(false);
    	} else if (!includeMinus && !includePlus){
    		assert(includeTimes);
    		timesBox.setClickable(false);
    	} else{
    		plusBox.setClickable(true);
    		minusBox.setClickable(true);
    		timesBox.setClickable(true);
    	}
    }
    
    private void generateExample(){
    	List<MathUtils.Operator> operators = new LinkedList<MathUtils.Operator>();
        if (includePlus) operators.add(MathUtils.Operator.ADDITION);
        if (includeMinus) operators.add(MathUtils.Operator.SUBTRACTION);
        if (includeTimes) operators.add(MathUtils.Operator.MULTIPLICATION);
        if (operators.isEmpty()) {
        	Log.w("SafeMode - Settings", "All operations excluded...");
        	return;
        }
        Solvable expr = MathUtils.generateProblem(operators, numDigits, exprLength);
        try {
			example.setText(expr.getHumanReadableEquation());
		} catch (MathUtilException e) {
			Log.e("SafeMode - ExprGen","Failed to generate expression",e);
			example.setText(getString(R.string.sett_err_expr_gen));
		}
    }
}
