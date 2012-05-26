package com.teamPrime.sm.history;

import android.app.Dialog;

import com.teamPrime.sm.HistoryActivity;

public interface DialogCreator {
	public Dialog createDialog(HistoryActivity activity, int dialogSubId);
}
