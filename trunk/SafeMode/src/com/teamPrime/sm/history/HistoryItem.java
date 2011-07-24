package com.teamPrime.sm.history;

import java.io.Serializable;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.TextView;

public abstract class HistoryItem implements Serializable{
	private static final long serialVersionUID = -7064460474245401203L;
	protected SMAction defaultAction; //Used for press events
	protected List<SMAction> actions; //Used for long press events
	
	public String toString(){
		return "History Item";
	}
	
	public void onClick(){
		defaultAction.execute();
	}
	public Dialog getLongPressDialog(Context c){
		final Dialog d = new Dialog(c);
		d.setTitle("Select Action:");
		ListView lv = new ListView(c);
		TextView tv = new TextView(c);
		tv.setText(defaultAction.toString());
		tv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				defaultAction.execute();
				d.cancel();
			}
		});
		lv.addView(tv);
		for (final SMAction act : actions){
			tv = new TextView(c);
			tv.setText(act.toString());
			tv.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					act.execute();
					d.cancel();
				}
			});
		}
		d.setContentView(lv);
		return d;
	}
}
