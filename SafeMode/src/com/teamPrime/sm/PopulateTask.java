package com.teamPrime.sm;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class PopulateTask extends AsyncTask<Void, Integer, Void> {
	private BlackListActivity mActivity;
	private boolean listPopulated = false;

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
    	//listPopulated = false;
    }

    // Runs on main thread.
    @Override
    protected void onProgressUpdate(Integer... count) {
    }

    // Runs on main thread.
    @Override
    protected void onPostExecute(Void result) {
    	mActivity.mArrayAdapterAC = new ArrayAdapter<String>(mActivity,android.R.layout.simple_dropdown_item_1line,mActivity.contactNames);
    	mActivity.mAutoComplete = (AutoCompleteTextView)mActivity.findViewById(R.id.blacklist_text);
        mActivity.mAutoComplete.setAdapter(mActivity.mArrayAdapterAC);
        mActivity.mAutoComplete.setThreshold(2);
    	
    }
	
	@Override
	protected Void doInBackground(Void... arg0) {
		if(mActivity!=null)
			mActivity.populatePeopleList();
		return null;
	}
	
}
