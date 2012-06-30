/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.tasks;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.teamPrime.sm.BlackListActivity;
import com.teamPrime.sm.R;

/**
 * PopulateTask populates the auto complete behind
 * the scenes for the BlackListActivity
 * 
 * @author Boris Treskunov
 * @version 1.0
 */
public class PopulateTask extends AsyncTask<Void, Integer, Void> {
	private BlackListActivity mActivity;
	ProgressDialog loading;

    public PopulateTask(BlackListActivity activity) {
        mActivity = activity;
    }

    // Can change activity this task points to.
    // e.g. when activity recreated after orientation change.
    public void setActivity(BlackListActivity activity) {
        mActivity = activity;
    }

    // Runs on main thread.
    @Override
    protected void onPreExecute() {
    	loading = ProgressDialog.show(mActivity, "",mActivity.getString(R.string.loading), true); 
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(Integer... count) {
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    	mActivity.setArrayAdapter(new ArrayAdapter<String>(mActivity,android.R.layout.simple_dropdown_item_1line,mActivity.getContactNames()));
    	mActivity.setAutoComplete((AutoCompleteTextView)mActivity.findViewById(R.id.blacklist_text));
        mActivity.getAutoComplete().setAdapter(mActivity.getArrayAdapter());
        mActivity.getAutoComplete().setThreshold(2);
        try{
        	loading.dismiss();
        }
        catch (Exception e){
        	
        }
    }
	
	@Override
	protected Void doInBackground(Void... arg0) {
		if(mActivity!=null)
			mActivity.populatePeopleList();
		return null;
	}
	
}
