package com.teamPrime.sm;

import java.util.LinkedList;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class BlackListActivity extends Activity {
    /** Called when the activity is first created. */
	
	ListView mBlackList;
	EditText mTextInput;
	Button mAddButton;
	Button mRemoveButton;
	Button mAddAll;
	Button mRemoveAll;
	ArrayAdapter<String> mListAdapter;
	LinkedList<String> contacts;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.black_list);
      
        mBlackList = (ListView) findViewById(R.id.blacklist_list);
        mTextInput = (EditText) findViewById(R.id.blacklist_text);    
        mAddButton = (Button) findViewById(R.id.add);
        mRemoveButton = (Button) findViewById(R.id.add);
        mAddAll = (Button) findViewById(R.id.add);
        mRemoveAll = (Button) findViewById(R.id.add);
        contacts = new LinkedList<String>();
        //contacts.add("test");
        
        mListAdapter = new ArrayAdapter<String>(this, R.layout.black_list, contacts);
        
        mBlackList.setAdapter(mListAdapter);
        
        mAddButton.setOnClickListener(new OnClickListener(){    	 	
    	public void onClick(View v){
    		mListAdapter.add(mTextInput.getText().toString());
    		//mBlackList.setAdapter(mListAdapter);
    	}
    });
        
        mRemoveButton.setOnClickListener(new OnClickListener(){    	 	
        	public void onClick(View v){
       
        	}
        });
        
        mAddAll.setOnClickListener(new OnClickListener(){    	 	
        	public void onClick(View v){
       
        	}
        });
        
        mRemoveAll.setOnClickListener(new OnClickListener(){    	 	
        	public void onClick(View v){
       
        	}
        });
}
}