/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.tasks;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.teamPrime.sm.R;

public class TemplateLoadingTask extends AsyncTask<Void, Void, Integer>{
	public static final int READ_MODE = 0;
	public static final int WRITE_MODE = 1;
	public static final int APPEND_MODE = 2;
	
	static final String SAVE_LOC = "SafeMode - FindMe_Text_Templates";
	
	private Activity mActivity;
	private final List<String> templates;
	private ProgressDialog loading;
	private final int mode;
	
	public TemplateLoadingTask(Activity activity, List<String> templates, int mode){
		mActivity = activity;
		this.templates = templates;
		this.mode = mode;
	}
	
	public int getMode(){
		return mode;
	}

	@Override
    protected void onPreExecute() {
    	if (mode == READ_MODE){
    		loading = ProgressDialog.show(mActivity, "",mActivity.getString(R.string.find_loading), true);
    	}
    }
	@Override
	protected Integer doInBackground(Void... arg0) {
		int numTemplates;
		switch(mode){
		case READ_MODE:
			numTemplates = SynchronizedTemplateAccessor.read(templates, mActivity);
			break;
		case WRITE_MODE:
			numTemplates = SynchronizedTemplateAccessor.write(templates, mActivity);
			break;
		case APPEND_MODE:
			numTemplates = SynchronizedTemplateAccessor.append(templates, mActivity);
			break;
		default:
			numTemplates = -1;
		}
		return numTemplates;
		
	}
	@Override
    protected void onPostExecute(Integer result) {
		if (mode == READ_MODE){
			try{loading.dismiss();}
			catch(Exception ex){}
		}
	}
}

class SynchronizedTemplateAccessor {
	public synchronized static int read(List<String> templates, Context mActivity){
		SharedPreferences info = mActivity.getSharedPreferences(TemplateLoadingTask.SAVE_LOC, Context.MODE_PRIVATE);
		int numTemplates = info.getInt("numTemplates", 0);
		for (int i = 1; i<=numTemplates; i++){
			String template = info.getString("TEMPLATE_"+i, null);
			if (template != null) templates.add(template);
		}
		if (templates.isEmpty()){
			templates.add(mActivity.getString(R.string.find_defText1));
			templates.add(mActivity.getString(R.string.find_defText2));
			templates.add(mActivity.getString(R.string.find_defText3));
			templates.add(mActivity.getString(R.string.find_defText4));
			templates.add(mActivity.getString(R.string.find_defText5));
			templates.add(mActivity.getString(R.string.find_defText6));
			write(templates, mActivity);
		}
		return numTemplates;
	}
	public synchronized static int write(List<String> templates, Context mActivity){
		SharedPreferences.Editor edit = mActivity.getSharedPreferences(TemplateLoadingTask.SAVE_LOC, Context.MODE_PRIVATE).edit();
		int numTemplates = templates.size();
		edit.putInt("numTemplates", numTemplates);
		for (int i = 1; i<=numTemplates; i++){
			edit.putString("TEMPLATE_"+i,templates.get(i-1));
		}
		edit.commit();
		return numTemplates;
	}
	public synchronized static int append(List<String> templates, Context mActivity){
		SharedPreferences info = mActivity.getSharedPreferences(TemplateLoadingTask.SAVE_LOC, Context.MODE_PRIVATE);
		SharedPreferences.Editor edit = info.edit();
		int numTemplates = info.getInt("numTemplates", 0);
		int start = numTemplates+1;
		numTemplates += templates.size();
		edit.putInt("numTemplates", numTemplates);
		for (int i = start; i<=numTemplates; i++){
			edit.putString("TEMPLATE_"+i, templates.get(i-start));
		}
		edit.commit();
		return numTemplates;
	}
}
