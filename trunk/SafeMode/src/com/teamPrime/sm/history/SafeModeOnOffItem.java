package com.teamPrime.sm.history;

import com.teamPrime.sm.HistoryActivity;

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
		return "SafeMode "+ (on ? "activated" : temp ? "paused" : "deactivated") + "\n" + getDate();
	}
	public String getTitle(){
		return "SafeMode "+ (on ? "activated" : temp ? "paused" : "deactivated");
	}
	public String getDescription(){
		return "";
	}

}
