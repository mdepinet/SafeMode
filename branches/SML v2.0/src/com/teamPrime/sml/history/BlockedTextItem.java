package com.teamPrime.sml.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.PhoneNumberUtils;

import com.teamPrime.sml.HistoryActivity;
import com.teamPrime.sml.R;

public class BlockedTextItem extends HistoryItem {
	private static final long serialVersionUID = 3581778324090032221L;
	
	private Date creationDate = new Date();
	private String phoneNumber;
	
	public BlockedTextItem(HistoryActivity activity, String phoneNumber, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		this.phoneNumber = phoneNumber;
		if (!this.phoneNumber.contains("-")) this.phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
	}
	
	public String toString(){
		return (activity==null?"Blocked text to ":activity.getString(R.string.hist_bText_bTextTo)+" ") + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return activity==null?"Blocked Text":activity.getString(R.string.hist_bText_bText);
	}
	
	public String getDescription(){
		//should be changed to contact name
		return PhoneNumberUtils.formatNumber(phoneNumber);
	}
}
