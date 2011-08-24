package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.PhoneNumberUtils;

import com.teamPrime.sm.HistoryActivity;

public class BlockedTextItem extends HistoryItem {
	private static final long serialVersionUID = 3581778324090032221L;
	
	private Date creationDate = new Date();
	private String phoneNumber;
	
	public BlockedTextItem(HistoryActivity activity, String phoneNumber, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		this.phoneNumber = phoneNumber;
	}
	
	public String toString(){
		return "Blocked Text to " + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return "Blocked Text";
	}
	
	public String getDescription(){
		//should be changed to contact name
		return PhoneNumberUtils.formatNumber(phoneNumber);
	}
}
