/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.teamPrime.sm.tasks.TemplateLoadingTask;

public class TextTemplateManagementActivity extends ListActivity {
	private static final int CONFIRM_DELETE_DIALOG_ID = 0;
	private static final int ADD_MESSAGE_DIALOG_ID = 1;
	
	private ArrayAdapter<String> templatesAdapter;
	private List<String> templates;
	private TemplateLoadingTask mTask;
	private int selectedItem = -1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_template_management);
        templates = new LinkedList<String>();
        mTask = new TemplateLoadingTask(this,templates,TemplateLoadingTask.READ_MODE);
        mTask.execute();
        try {
			mTask.get();
		} catch (InterruptedException e) {
			Log.e("SAFEMODE - FindMe Templates","Failed to load templates",e);
		} catch (ExecutionException e) {
			Log.e("SAFEMODE - FindMe Templates","Failed to load templates",e);
		}
        templatesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, templates);
        setListAdapter(templatesAdapter);
        
        getListView().setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long id) {
				return onListItemLongClick(v, pos, id);
			}
        });
	}
	
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    @Override
    public void onPause(){
        super.onPause();
        if (mTask.getMode() == TemplateLoadingTask.READ_MODE) mTask.cancel(true);
        mTask = new TemplateLoadingTask(this,templates,TemplateLoadingTask.WRITE_MODE);
        mTask.execute((Void[]) null);
    }
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    } 
    
    protected boolean onListItemLongClick(View v, int position, long id){
    	selectedItem = position;
    	showDialog(CONFIRM_DELETE_DIALOG_ID);
    	return true;
    }
    
    @Override
	public Dialog onCreateDialog(int id){
		Dialog d = null;
		switch(id){
		case CONFIRM_DELETE_DIALOG_ID:
			d = new AlertDialog.Builder(this).setMessage(getString(R.string.find_confirm_delete))
		           .setCancelable(false)
		           .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener(){
		        	   public void onClick(DialogInterface dialog, int id) {
		        		   if (selectedItem >= 0 && selectedItem < templates.size())
		        			   templatesAdapter.remove(templates.get(selectedItem));
		        		   selectedItem = -1;
		        		   dialog.dismiss();
		        	   }
		           })
		           .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
		               public void onClick(DialogInterface dialog, int id) {
		            	   selectedItem = -1;
		                   dialog.cancel();
		               }
		           }).create();
			break;
		case ADD_MESSAGE_DIALOG_ID:
			final Dialog addDialog = new Dialog(this);
			addDialog.setContentView(R.layout.add_text_template);
			addDialog.setTitle(R.string.temp_title);
			addDialog.setCancelable(true);
			addDialog.setCanceledOnTouchOutside(true);
			addDialog.setOnDismissListener(new OnDismissListener(){
				@Override
				public void onDismiss(DialogInterface arg0) {
					((EditText) addDialog.findViewById(R.id.newTemplate)).setText("");
				}
			});
			((Button)(addDialog.findViewById(R.id.addTemplateButton))).setOnClickListener(new AddTemplateListener(addDialog,true));
			((Button)(addDialog.findViewById(R.id.addTemplateCancelButton))).setOnClickListener(new AddTemplateListener(addDialog,false));
			d = addDialog;
			break;
		}
		return d;
	}
    
    class AddTemplateListener implements OnClickListener{
    	private Dialog d;
    	private boolean addTemplate = true;
    	
    	public AddTemplateListener(Dialog d, boolean addTemplate){
    		this.d = d;
    		this.addTemplate = addTemplate;
    	}
    	
    	@Override
		public void onClick(View v) {
			if (!addTemplate) d.cancel();
			else{
				String s = ((TextView)(((View)((View)v.getParent()).getParent()).findViewById(R.id.newTemplate))).getText().toString();
				templatesAdapter.add(s);
				d.dismiss();
			}
		}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	boolean result = super.onCreateOptionsMenu(menu);
    	menu.add(Menu.NONE, Menu.NONE, Menu.NONE, getString(R.string.temp_title));
        return result;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	showDialog(ADD_MESSAGE_DIALOG_ID);
        return super.onOptionsItemSelected(item);
    }
    
    
}
