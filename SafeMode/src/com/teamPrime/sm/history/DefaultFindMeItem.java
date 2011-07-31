package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamPrime.sm.HistoryActivity;

public class DefaultFindMeItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	private Date creationDate = new Date();
	private String phoneNumber;
	
	public DefaultFindMeItem(HistoryActivity activity, String phoneNumber, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		this.phoneNumber = phoneNumber;
	}
	
	public String toString(){
		return "Find Me Text to " + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return "Find Me Text";
	}
	
	public String getDate(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(0, 10);
	}
	
	public String getTime(){
		return new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate).substring(10);
	}
}
