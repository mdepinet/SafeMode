package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamPrime.sm.HistoryActivity;

public class DateItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	private Date creationDate = new Date();
	
	public DateItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
	}
	
	public String toString(){
		return "This is a date item";
	}
	
	public String getTitle(){
		return "Date Item";
	}
	
	public String getDescription(){
		//should be changed to contact name
		return "date";
	}
	
	public String getDate(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(0, 10);
	}
	
	public String getTime(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(10);
	}
}
