/**
 * Copyright � 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.history;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.R;

public class SafeModeOnOffItem extends HistoryItem {
	private static final long serialVersionUID = 6608424493099107410L;
	
	private boolean on = true;
	private boolean temp = false;
	
	public SafeModeOnOffItem(boolean onItem, HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		on = onItem;
	}
	public SafeModeOnOffItem(boolean onItem, boolean tempOff, HistoryActivity activity, HistAction defaultAction, HistAction... acts){
		super(activity, defaultAction, acts);
		on = onItem;
		temp = tempOff;
	}
	
	public String toString(){
		String activated = "activated";
		String paused = "paused";
		String deactivated = "deactivated";
		if (activity!=null){
			activated = activity.getString(R.string.hist_activated);
			paused = activity.getString(R.string.hist_paused);
			deactivated = activity.getString(R.string.hist_deactivated);
		}
		return "SafeMode "+ (on ? activated : temp ? paused : deactivated) + "\n" + getDate();
	}
	public String getTitle(){
		String activated = "activated";
		String paused = "paused";
		String deactivated = "deactivated";
		if (activity!=null){
			activated = activity.getString(R.string.hist_activated);
			paused = activity.getString(R.string.hist_paused);
			deactivated = activity.getString(R.string.hist_deactivated);
		}
		return "SafeMode "+ (on ? activated : temp ? paused : deactivated);
	}
	public String getDescription(){
		return "";
	}

}
