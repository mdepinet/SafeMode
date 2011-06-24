package com.teamPrime.sm.tasks;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.teamPrime.sm.BlackListActivity;
import com.teamPrime.sm.R;

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
    	loading = ProgressDialog.show(mActivity, "","Loading Contacts...", true); 
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
        loading.dismiss();
    }
	
	@Override
	protected Void doInBackground(Void... arg0) {
		if(mActivity!=null)
			mActivity.populatePeopleList();
		return null;
	}
	
}
