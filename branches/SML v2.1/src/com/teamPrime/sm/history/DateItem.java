/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.history;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.R;

public class DateItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	
	public DateItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
	}
	
	public String toString(){
		return (activity==null?"Date: ":activity.getString(R.string.hist_date)) +getDate();
	}
	
	public String getTitle(){
		return getDate();
	}
	
	public String getDescription(){
		return "";
	}
}
