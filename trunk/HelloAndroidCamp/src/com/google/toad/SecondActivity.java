package com.google.toad;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SecondActivity extends Activity{
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view2);
		
		Button but1 = (Button)findViewById(R.id.second_button1);
		but1.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
			}
		});
	}
}
