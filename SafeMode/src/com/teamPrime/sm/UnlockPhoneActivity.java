package com.teamPrime.sm;


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UnlockPhoneActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.unlockphone);
	        TextView rView = (TextView)findViewById(R.id.random);
	        TextView hView = (TextView)findViewById(R.id.hidden_answer);
	        
	        int a, b, answer;
	        a = generateRand(10, 20);
	        b = generateRand(10, 20);
	        
	        answer = a*b;
	        
	        rView.setText("What is " + a + " x " + b + "?");
	        hView.setText(Integer.toString(answer));
    	} catch(Exception e) {
    		
    	}
    }
    
    public int generateRand(int lower, int upper) {
		return 2;
    }
    
    /** Handler to receive onClick events from buttons.
    *
    * This is defined on a per-button basis inside of layout/main.xml by setting
    * android:onClick="onClick".
    */
   public void onClick(View v) {
       switch (v.getId()) {
           case R.id.submit_button:
        	   TextView ansView = (TextView) findViewById(R.id.hidden_answer);
        	   TextView inputAns = (TextView) findViewById(R.id.input_answer);
        	   
        	   String a_temp = ansView.getText().toString();
        	   int ans = Integer.parseInt(a_temp);
        	   
        	   String i_temp = inputAns.getText().toString();
        	   int inp = Integer.parseInt(i_temp);
        	   
        	   // correct answer was given
        	   if(ans == inp) {
        		   inputAns.setText("Correct answer was given.");
        	   }
        	   
        	   // incorrect answer was given
        	   else {
        		   inputAns.setText("Incorrect answer was given.");
        	   }
           break;
       }
   }
}