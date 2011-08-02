package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamPrime.sm.HistoryActivity;

public class DateItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	
	public DateItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
	}
	
	public String toString(){
		return "This is a date item";
	}
	
	public String getTitle(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getDescription(){
		return "";
	}
	
	public String getDate(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(0, 10);
	}
	
	public String getTime(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(10);
	}
}
