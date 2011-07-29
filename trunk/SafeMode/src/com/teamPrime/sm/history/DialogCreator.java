package com.teamPrime.sm.history;

import android.app.Dialog;
import android.content.Context;

public interface DialogCreator {
	public Dialog createDialog(Context c, int dialogSubId);
}
