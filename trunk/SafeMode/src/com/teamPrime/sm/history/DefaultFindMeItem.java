package com.teamPrime.sm.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.teamPrime.sm.HistoryActivity;

public class DefaultFindMeItem extends HistoryItem {
	private static final long serialVersionUID = -361038583952900332L;
	private Date creationDate = new Date();
	
	public DefaultFindMeItem(HistoryActivity activity, HistAction defaultAction, HistAction... acts) {
		super(activity, defaultAction, acts);
	}
	
	public String toString(){
		return "Find Me Text\n"+new SimpleDateFormat(HistoryItem.DATE_FORMAT).format(creationDate);
	}
}
