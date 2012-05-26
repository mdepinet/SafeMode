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
	
	private static final String SAVE_LOC = "SafeMode - FindMe_Text_Templates";
	
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
			SharedPreferences info = mActivity.getSharedPreferences(SAVE_LOC, Context.MODE_PRIVATE);
			numTemplates = info.getInt("numTemplates", 0);
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
			}
			else break; //Don't break if templates was empty so that the defaults get written too
		case WRITE_MODE:
			SharedPreferences.Editor edit = mActivity.getSharedPreferences(SAVE_LOC, Context.MODE_PRIVATE).edit();
			numTemplates = templates.size();
			edit.putInt("numTemplates", numTemplates);
			for (int i = 1; i<=numTemplates; i++){
				edit.putString("TEMPLATE_"+i,templates.get(i-1));
			}
			edit.commit();
			break;
		case APPEND_MODE:
			SharedPreferences info2 = mActivity.getSharedPreferences(SAVE_LOC, Context.MODE_PRIVATE);
			SharedPreferences.Editor edit2 = info2.edit();
			int start = (numTemplates = info2.getInt("numTemplates", 0))+1;
			numTemplates += templates.size();
			edit2.putInt("numTemplates", numTemplates);
			for (int i = start; i<=numTemplates; i++){
				edit2.putString("TEMPLATE_"+i, templates.get(i-start));
			}
			edit2.commit();
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
