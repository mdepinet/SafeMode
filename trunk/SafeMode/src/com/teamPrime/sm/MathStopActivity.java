package com.teamPrime.sm;

import java.util.Random;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MathStopActivity extends Activity{
	
	private final int[] mult1Choices = {3, 4, 6, 7, 8, 9};
	
	private TextView question;
	private TextView answer;
	private int mult1, mult2, corAnswer;
	@SuppressWarnings("unused")
	private boolean onState;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.math_stop);
        Random r = new Random();
        int  mult1Index = r.nextInt(6);
        mult1 = mult1Choices[mult1Index];
        mult2 = r.nextInt(8);
        mult2 += 12;
        String mathQuestion = String.format("What is %d * %d?", mult1, mult2);
        question = (TextView) findViewById(R.id.questionStopText);
        question.setText(mathQuestion);
        corAnswer = mult1 * mult2;
        answer = (TextView) findViewById(R.id.answerScreenStop);
        SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
        onState = data.getBoolean("onState", true);
        
    }

    /** Called upon subsequent calls to Activity **/
    
    @Override
   protected void onResume() {
    	super.onResume();
    	Random r = new Random();
    	mult1 = r.nextInt(5);
    	mult1 += 3;
    	mult2 = r.nextInt(8);
    	mult2 += 12;
    	String mathQuestion = String.format("What is %d * %d?", mult1, mult2);
    	question.setText(mathQuestion);
    	corAnswer = mult1 * mult2;
    }
    
    public void onClick(View v) {
    	if (v.getId() == R.id.submitStopButton) {
    		String drunkAnsString = answer.getText().toString();
    		try {
    			int drunkAns = Integer.parseInt(drunkAnsString);
    			if (drunkAns == corAnswer) {
    				//if (onState == true) {
    					SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
    					SharedPreferences.Editor editor = data.edit();
    					editor.putBoolean("onState", false);
    					onState = false;
    					editor.commit();
    					Toast.makeText(getApplicationContext(), "SAFEMODE turned off", Toast.LENGTH_SHORT).show();
    					this.finish();
    				//}
    			}
    			else {
    				Toast.makeText(getApplicationContext(), "Wrong Answer", Toast.LENGTH_SHORT).show();
    				this.finish();
    			}
    		}
    		catch (NumberFormatException e) {
    			Toast.makeText(getApplicationContext(), "Wrong Answer", Toast.LENGTH_SHORT).show();
    			this.finish();
    			
    		}
    		
    	}
    }
    
}
