package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.PhoneNumberUtils;

import com.teamPrime.sm.HistoryActivity;

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
		return "Find Me Text to " + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return "Find Me Text";
	}
	
	public String getDescription(){
		return contactName;
	}
}
