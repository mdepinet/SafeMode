/**
 * Copyright © 2011 Boris Treskunov
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.tasks;


import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.teamPrime.sm.FindMeActivity;
import com.teamPrime.sm.R;

public class PopulateTaskFindMe extends AsyncTask<Void, Integer, Void> {
	private FindMeActivity mActivity;
	ProgressDialog loading;

    public PopulateTaskFindMe(FindMeActivity activity) {
        mActivity = activity;
    }

    // Can change activity this task points to.
    // e.g. when activity recreated after orientation change.
    public void setActivity(FindMeActivity activity) {
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
    	//mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    	mActivity.populateList();
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
