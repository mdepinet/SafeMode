package com.teamPrime.sml.history;

import android.app.Dialog;

import com.teamPrime.sml.HistoryActivity;

public interface DialogCreator {
	public Dialog createDialog(HistoryActivity activity, int dialogSubId);
}
