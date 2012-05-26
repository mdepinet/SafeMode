package com.teamPrime.sml.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.telephony.PhoneNumberUtils;

import com.teamPrime.sml.HistoryActivity;
import com.teamPrime.sml.R;

public class BlockedCallItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	private Date creationDate = new Date();
	private String phoneNumber;
	
	public BlockedCallItem(HistoryActivity activity, String phoneNumber, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		this.phoneNumber = phoneNumber;
		if (!this.phoneNumber.contains("-")) this.phoneNumber = android.telephony.PhoneNumberUtils.formatNumber(phoneNumber);
	}
	
	public String toString(){
		return (activity==null?"Blocked call to ":activity.getString(R.string.hist_bCall_bCallTo)+" ") + phoneNumber + "\n" + new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
	
	public String getTitle(){
		return activity==null?"Blocked Call":activity.getString(R.string.hist_bCall_bCall);
	}
	
	public String getDescription(){
		//should be changed to contact name
		return PhoneNumberUtils.formatNumber(phoneNumber);
	}
}
