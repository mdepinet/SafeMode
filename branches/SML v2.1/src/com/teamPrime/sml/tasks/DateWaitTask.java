/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sml.tasks;


import android.os.AsyncTask;

import com.teamPrime.sml.SafeLaunchActivity;

/**
 * DateWaitTask allows SafeLaunchActivity to wait
 * for the user to enter a date and time without
 * force closing before then.
 * 
 * @author Mike Depinet
 * @version 1.0
 */
public class DateWaitTask extends AsyncTask<Void, Integer, Void> {
	private SafeLaunchActivity mActivity;
	private boolean dateUpdated, timeUpdated = false;

    public DateWaitTask(SafeLaunchActivity activity) {
        mActivity = activity;
    }

    // Can change activity this task points to.
    // e.g. when activity recreated after orientation change.
    public void setActivity(SafeLaunchActivity activity) {
        mActivity = activity;
    }

    // Runs on main thread.
    @Override
    protected void onPreExecute() {
    	timeUpdated = false;
    	dateUpdated = false;
    	mActivity.showDialog(SafeLaunchActivity.DATE_ID);
    	mActivity.showDialog(SafeLaunchActivity.TIME_ID);
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(Integer... count) {
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    	mActivity.finishTurnOn();
    }
	
	@Override
	protected Void doInBackground(Void... arg0) {
		while (!timeUpdated || !dateUpdated){
			timeUpdated |= mActivity.isTimeUpdated();
			dateUpdated |= mActivity.isDateUpdated();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		return null;
	}
	
}
