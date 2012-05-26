package com.teamPrime.sm.history;

import java.io.Serializable;

import com.teamPrime.sm.HistoryActivity;

/**
 * HistAction is used to execute custom code when a history item is selected.
 * This can be used to fire an intent, finish the activity, etc.
 * 
 * @author Mike Depinet
 *
 */
public interface HistAction extends Serializable{
	public void execute(HistoryActivity activity);
}
