/**
 * Copyright � 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.history;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.R;

public abstract class HistoryItem implements Serializable, DialogCreator{
	private static final long serialVersionUID = -7064460474245401203L;
	
	public static final String DATE_FORMAT = "M/d/yyyy hh:mm aa";
	private Date creationDate = new Date();
	
	protected List<HistAction> actions; //Actions available for this item
	protected HistoryActivity activity; //Used for dialog creation
	
	public HistoryItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts){
		this.activity = activity;
		actions = new ArrayList<HistAction>();
		actions.add(defaultAction);
		for (HistAction ha : acts){
			actions.add(ha);
		}
	}
	
	public void setActivity(HistoryActivity activity){
		this.activity = activity;
	}
	
	public String toString(){
		return "History Item";
	}
	
	public void onClick(){
		if (activity != null) activity.showDialog(this, -1); //emptyItem will not do anything
	}

	public Dialog createDialog(final HistoryActivity activity, int subDialogId){
		if (actions == null || actions.isEmpty()) return null;
		boolean allNull = true;
		for (HistAction hist : actions){
			if (hist != null){
				allNull = false;
				break;
			}
		}
		if (allNull) return null;
		final HistAction[] combinedActions = new HistAction[actions.size()];
		String[] titles = new String[combinedActions.length];
		for (int i = 0; i<actions.size(); i++){
			combinedActions[i] = actions.get(i);
			titles[i] = actions.get(i).toString();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(""+toString()).setItems(titles, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				dialog.dismiss();
				combinedActions[item].execute(activity);
		 }
		}).setCancelable(true);
		return builder.create();
	}
	
	public String getTitle(){
		return "";
	}
	
	public String getDescription(){
		return activity==null?"Your History is Currently Empty...":activity.getString(R.string.hist_empty);
	}
	
	public Date getCreationDate(){
		return creationDate;
	}
	public void setCreationDate(Date creationDate){
		this.creationDate = creationDate;
	}
	
	public String getDate(){
		String date = new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
		date = date.substring(0, date.indexOf(" "));
		return date;
	}
	
	public String getTime(){
		String date = new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
		date = date.substring(date.indexOf(" ")+1);
		return date;
	}
}
