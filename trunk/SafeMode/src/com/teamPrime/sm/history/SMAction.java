package com.teamPrime.sm.history;

import java.io.Serializable;

/**
 * SMAction is used to execute custom code when a history item is selected.
 * This can be used to fire an intent, finish the activity, etc.
 * 
 * @author Mike Depinet
 *
 */
public interface SMAction extends Serializable{
	public void execute();
}
