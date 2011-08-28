package com.teamPrime.sm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.teamPrime.sm.tasks.BlackListIOTask;

public class UserPresentReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences data = context.getSharedPreferences("SAFEMODE", Context.MODE_PRIVATE);
		boolean onState = data.getBoolean("onState", false);
		if (!onState) return;
		boolean tempOff = data.getBoolean("tempOff", false);
		if (tempOff){
			BlackListIOTask ioTask = new BlackListIOTask(context,null,BlackListIOTask.RESUME_CALL_TEXT_BLOCKING);
			ioTask.execute((Void[])null);
			SharedPreferences.Editor editor = data.edit();
			editor.putBoolean("tempOff", false);
			editor.commit();
			
			Notification not = new Notification(R.drawable.locked, context.getString(R.string.notif_locked_short), System.currentTimeMillis());
			not.setLatestEventInfo(context.getApplicationContext(), context.getString(R.string.notif_locked_message), context.getString(R.string.notif_click_here),
								  PendingIntent.getActivity(context.getApplicationContext(), 0, new Intent(context.getApplicationContext(), MathStopActivity.class), Intent.FLAG_ACTIVITY_NEW_TASK));
			not.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
			NotificationManager notMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			notMgr.notify(SafeLaunchActivity.LockedNotificationId, not);
			
			SafeLaunchActivity.setTimerAppendStar(false);
		}
	}
}
