package com.teamPrime.sm.history.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.telephony.SmsManager;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.history.DialogCreator;
import com.teamPrime.sm.history.HistAction;
import com.teamPrime.sm.tasks.BlackListIOTask;

public class ViewTextAction implements HistAction, DialogCreator {
	private static final long serialVersionUID = 8408088951105555088L;
	
	private String messageText;
	private String phoneNumber;
	private int responseCode;
	
	public ViewTextAction(String phoneNumber, String messageText, int responseCode){
		this.phoneNumber = phoneNumber;
		this.messageText = messageText;
		this.responseCode = responseCode;
	}
	
	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	public void execute(HistoryActivity activity){
		activity.showDialog(this, 0);
	}
	
	public Dialog createDialog(final HistoryActivity activity, int dialogSubId){
		String response = "Unknown";
		switch (responseCode){
		case Activity.RESULT_OK:
			response = "This message was sent successfully.";
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			response = "This message failed to send.";
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			response = "This message failed to send because you did not have service.";
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			response = "This message failed to send because your radio was off.";
			break;
		case BlackListIOTask.SafeMode_BLOCKED_SMS_RESPONSE_CODE:
			response = "This message was blocked by SafeMode";
			break;
		}
		return new AlertDialog.Builder(activity).setMessage("Sent to: "+phoneNumber+"\n\nMessage: "+messageText+"\n\nStatus: "+response)
		.setCancelable(false).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.dismiss();
	           }
		}).create();
	}
	
	public String toString(){
		return "View Text";
	}
}
