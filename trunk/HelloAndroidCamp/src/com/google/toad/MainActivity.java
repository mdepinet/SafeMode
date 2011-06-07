package com.google.toad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	Button mButton;
	final String TAG = "MainActivity";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.d(TAG,"Started MainActivity");
        mButton = (Button)findViewById(R.id.button1);
        mButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		//Toast.makeText(getApplicationContext(), "You clicked me!", Toast.LENGTH_LONG).show();
        		startActivity(new Intent(getApplicationContext(), SecondActivity.class));
        	}
        });
    }
}