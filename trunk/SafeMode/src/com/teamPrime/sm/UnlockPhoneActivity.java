package com.teamPrime.sm;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
	        a = MathUtils.genSmallRandom();
	        b = MathUtils.genLargeRandom();
	        
	        answer = a*b;
	        
	        rView.setText("What is " + a + " x " + b + "?");
	        hView.setText(Integer.toString(answer));


	        	
    	} catch(Exception e) {
    		Log.i("onCreate_error", "e: " + e);
    	}
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
        		   Toast.makeText(getApplicationContext(), "Correct Answer. Contacts unprotected.", Toast.LENGTH_SHORT).show();
					//SharedPreferences data = getSharedPreferences("SAFEMODE", MODE_PRIVATE);
					//SharedPreferences.Editor editor = data.edit();
					//editor.putBoolean("onState", false);
					//editor.commit();
					
					// get rid of notifications
					String ns = Context.NOTIFICATION_SERVICE;
					NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
					mNotificationManager.cancelAll();
					
				   this.finish();

        	   }
        	   
        	   // incorrect answer was given
        	   else {
        		   Toast.makeText(getApplicationContext(), "Incorrect Answer.", Toast.LENGTH_SHORT).show();
        		   this.finish();
        	   }
           break;
       }
   }
}