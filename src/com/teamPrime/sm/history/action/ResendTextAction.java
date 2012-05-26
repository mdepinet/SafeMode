package com.teamPrime.sm.history.action;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.history.HistAction;

public class ResendTextAction implements HistAction{
	private static final long serialVersionUID = -2975499624925840868L;
	
	private String messageText;
	private String phoneNumber;
	private boolean ignoreSafeMode;
	
	public ResendTextAction(String phoneNumber, String messageText, boolean ignoreSafeMode){
		this.phoneNumber = phoneNumber;
		this.messageText = messageText;
		this.ignoreSafeMode = ignoreSafeMode;
	}
	
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public void setIgnoreSafeMode(boolean ignoreSafeMode) {
		this.ignoreSafeMode = ignoreSafeMode;
	}
	
	public void execute(final HistoryActivity activity){
		SharedPreferences data = activity.getSharedPreferences("SAFEMODE", Context.MODE_PRIVATE);
		boolean onState = data.getBoolean("onState", true);
		if (onState && !ignoreSafeMode){
			Toast.makeText(activity, "Cannot resend while SafeMode is on", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String SENT = "SMS_SENT";
        String DELIVERED = "SMS_DELIVERED";
 
        PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0,
            new Intent(SENT), 0);
 
        PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0,
            new Intent(DELIVERED), 0);
 
        //---when the SMS has been sent---
        (activity).registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context arg0, Intent arg1) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(activity.getBaseContext(), "Text resent", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(activity.getBaseContext(), "An error occured, please try again", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(activity.getBaseContext(), "No service", 
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(activity.getBaseContext(), "Radio off, unable to send message", 
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));    
 
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, messageText, sentPI, deliveredPI);
	}
	
	public String toString(){
		return "Resend";
	}
}
