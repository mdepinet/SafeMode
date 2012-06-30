/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.history;

import android.app.Dialog;

import com.teamPrime.sm.HistoryActivity;

public interface DialogCreator {
	public Dialog createDialog(HistoryActivity activity, int dialogSubId);
}
