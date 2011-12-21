package com.teamPrime.sm.history;

import com.teamPrime.sm.HistoryActivity;

public class DateItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	
	public DateItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
	}
	
	public String toString(){
		return "Date: "+getDate();
	}
	
	public String getTitle(){
		return getDate();
	}
	
	public String getDescription(){
		return "";
	}
}
