package com.teamPrime.sm.history;

import com.teamPrime.sm.HistoryActivity;

public class SafeModeOnOffItem extends HistoryItem {
	private static final long serialVersionUID = 6608424493099107410L;
	
	private boolean on = true;
	
	public SafeModeOnOffItem(boolean onItem, HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
		on = onItem;
	}
	
	public String toString(){
		return "SafeMode "+ (on ? "activated" : "deactivated");
	}
	public String getTitle(){
		return "SafeMode "+ (on ? "activated" : "deactivated");
	}
	public String getDescription(){
		return "";
	}

}
