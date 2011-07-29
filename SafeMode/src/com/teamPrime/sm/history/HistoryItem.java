package com.teamPrime.sm.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.teamPrime.sm.HistoryActivity;

public abstract class HistoryItem implements Serializable, DialogCreator{
	private static final long serialVersionUID = -7064460474245401203L;
	
	public static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";
	
	protected HistAction defaultAction; //Used for press events
	protected List<HistAction> actions; //Used for long press events
	protected HistoryActivity activity; //Used for dialog creation
	
	public HistoryItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts){
		this.activity = activity;
		this.defaultAction = defaultAction;
		this.actions = new ArrayList<HistAction>();
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
		//activity.showDialog(this, -1);
		defaultAction.execute(activity);
	}

	public Dialog createDialog(Context c, int subDialogId){
		final Dialog d = new Dialog(c);
		d.setTitle("Select Action:");
		ListView lv = new ListView(c);
		TextView tv = new TextView(c);
		tv.setText(defaultAction.toString());
		tv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				defaultAction.execute(activity);
				d.cancel();
			}
		});
		lv.addView(tv);
		for (final HistAction act : actions){
			tv = new TextView(c);
			tv.setText(act.toString());
			tv.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					act.execute(activity);
					d.cancel();
				}
			});
		}
		d.setContentView(lv);
		return d;
	}
}
