/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

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
