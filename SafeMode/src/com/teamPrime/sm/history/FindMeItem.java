package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.R;

public class FindMeItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	private Date creationDate = new Date();
	private String phoneNumber;
	private String contactName;
	
	public FindMeItem(HistoryActivity activity, String phoneNumber, String contactName, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		this.phoneNumber = phoneNumber;
		this.contactName = contactName;
	}
	
	public String toString(){
		return (activity==null?"Find Me Text to ":activity.getString(R.string.hist_fmText_fmTextTo)) + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return activity==null?"Find Me Text":activity.getString(R.string.hist_fmText_fmText);
	}
	
	public String getDescription(){
		return contactName;
	}
}
