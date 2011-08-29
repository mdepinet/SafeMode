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

public abstract class HistoryItem implements Serializable, DialogCreator{
	private static final long serialVersionUID = -7064460474245401203L;
	
	public static final String DATE_FORMAT = "M/d/yyyy hh:mm aa";
	private Date creationDate = new Date();
	
	protected List<HistAction> actions; //Actions available for this item
	protected HistoryActivity activity; //Used for dialog creation
	
	public HistoryItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts){
		this.activity = activity;
		this.actions = new ArrayList<HistAction>();
		actions.add(defaultAction);
		for (HistAction ha : acts){
			this.actions.add(ha);
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
		//defaultAction.execute(activity);
	}

	public Dialog createDialog(final HistoryActivity activity, int subDialogId){
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
		return "Your History is Currently Empty...";
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
