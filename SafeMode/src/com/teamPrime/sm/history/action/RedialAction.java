/**
 * Copyright � 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.history.action;

import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.history.HistAction;

public class RedialAction implements HistAction{
	private static final long serialVersionUID = -3193720366235758845L;
	
	private String phoneNumber;
	
	public RedialAction(String phoneNumber){
		this.phoneNumber = phoneNumber;
		if (!this.phoneNumber.contains("-")) this.phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		if (!this.phoneNumber.contains("-")) this.phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);
	}
	
	public void execute(final HistoryActivity activity){
		Intent i = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+phoneNumber));
		activity.startActivity(i);
	}
	
	public String toString(){
		return "Redial";
	}
}
