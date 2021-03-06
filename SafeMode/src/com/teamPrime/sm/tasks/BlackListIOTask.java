/**
 * Copyright � 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.tasks;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.teamPrime.sm.HistoryActivity;
import com.teamPrime.sm.data.ContactDAO;
import com.teamPrime.sm.data.ContactData;
import com.teamPrime.sm.history.BlockedCallItem;
import com.teamPrime.sm.history.BlockedTextItem;
import com.teamPrime.sm.history.action.RedialAction;
import com.teamPrime.sm.history.action.ResendTextAction;
import com.teamPrime.sm.history.action.ViewTextAction;

/**
 * BlackListIOTask takes care of calling ContactDAO in the
 * background with the proper contact ids.
 * 
 * @author Mike Depinet
 * @version 2.0
 */
public class BlackListIOTask extends AsyncTask<Void, Void, List<Long>> {
	public static final int WRITE_IDS_MODE = 0;
	public static final int READ_IDS_MODE = 1;
	public static final int HIDE_CONTACTS_MODE = 2; //Note that this must also read ids
	public static final int REVEAL_CONTACTS_MODE = 3;
	public static final int CONTINUE_CONTACTS_REVEAL = 6;
	
	public static final int SafeMode_BLOCKED_SMS_RESPONSE_CODE = 101;
	
//	private static boolean savingContacts = false;
	
	private static final String fileName = "SAFEMODE_contactIds.bin";
	private static final String smsUri = "content://sms";
	
	private Context mActivity;
	private int mode;
	private List<Long> contactIds;
	
	private static Handler mHandler;
	private static Context callTextBlockActivity;
	private static CallInterceptor callInt;
	private static SmsInterceptor smsInt;
	private static Collection<String> blockedNums = null;
	
//	public static boolean isSavingContacts(){
//		return savingContacts;
//	}
	
	public BlackListIOTask(Context activity, List<Long> contactIds, int mode){
		mActivity = activity;
		this.contactIds = contactIds;
		this.mode = mode;
	}
	public void setActivity(Context activity){
		mActivity = activity;
	}
	
	 //Runs on main thread.
	 @Override
	 protected void onPreExecute() {
//		 if (mode > 1 && savingContacts){
//			 cancel(true); //We actually need a task queue
//			 return;
//		 }
		 if (mHandler == null) mHandler = new Handler();
//		 if (mode > 1) savingContacts = true;
	 }

	 // Runs on main thread.
	 @Override
	 protected void onProgressUpdate(Void... arg0) {
	 }

	 // Runs on main thread.
	 @Override
	 protected void onPostExecute(List<Long> result) {
		 switch(mode){
		 case HIDE_CONTACTS_MODE:
			 startCallTextBlocking();
			 break;
		 }
//		 if (mode > 1) savingContacts = false;
	 }
	
	 @Override
	 protected List<Long> doInBackground(Void... arg0) {
		 switch(mode){
		 case WRITE_IDS_MODE:
			 ObjectOutputStream oos = null;
			 try {
				 oos = new ObjectOutputStream(mActivity.openFileOutput(fileName, Activity.MODE_PRIVATE));
				 oos.writeObject(contactIds);
			 } catch (FileNotFoundException e) {
				 Log.e("SAFEMODE - ContactsIO", "Failed to write contact ids", e);
			 } catch (IOException e) {
				 Log.e("SAFEMODE - ContactsIO", "Failed to write contact ids", e);
			 } finally{
				 try{oos.close();}catch(Throwable t){}
			 }
			 return null;
		 case READ_IDS_MODE:
			 readContactIds();
			 return contactIds;
		 case HIDE_CONTACTS_MODE:
			 readContactIds();
			 
			 int i = 0;
			 //Remove from database
			 List<ContactData> blockedData = ContactDAO.hideContacts(mActivity, contactIds);
			 blockedNums = ContactDAO.getPhoneNumbersForContacts(blockedData);
			 List<Long> result = new LinkedList<Long>();
			 result.add((long) i);
			 return result;
		 case REVEAL_CONTACTS_MODE:
			 int j = ContactDAO.revealContacts(mActivity);
			 try{
				 if (callInt != null) mActivity.unregisterReceiver(callInt);
			 } catch (IllegalArgumentException ex){} //If it is not registered
			 try{
				 if (smsInt != null) mActivity.getContentResolver().unregisterContentObserver(smsInt);
			 } catch (IllegalArgumentException ex){} //If it is not registered
			 List<Long> result2 = new LinkedList<Long>();
			 result2.add((long) j);
			 return result2;
		 case CONTINUE_CONTACTS_REVEAL:
			 List<Long> result3 = new LinkedList<Long>();
			 result3.add((long) ContactDAO.continueReveal(mActivity));
			 return result3;
		 default:
			 return null;
		 }
	 }
	 
	 @SuppressWarnings("unchecked")
	 private void readContactIds(){
		 ObjectInputStream ois = null;
		 try{
			 ois = new ObjectInputStream(mActivity.openFileInput(fileName));
			 contactIds = (List<Long>) ois.readObject();
		 } catch(IOException e) {
			 Log.e("SAFEMODE - ContactsIO", "Failed to read contact ids", e);
		 } catch (ClassNotFoundException e) {
			 Log.e("SAFEMODE - ContactsIO", "Failed to read contact ids", e);
		 } finally{
				try{ois.close();}catch(Throwable t){}
		 }
	 }
	 
	 private void startCallTextBlocking(){
		 if (callTextBlockActivity == null) callTextBlockActivity = mActivity;
		 
		 //Register receiver to explicitly block outgoing calls
		 IntentFilter filter = new IntentFilter();
	     filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
	     filter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY+1);
	     callTextBlockActivity.registerReceiver(callInt = new CallInterceptor(), filter);
		 
		 //Register content observer to explicitly block outgoing sms messages
	     callTextBlockActivity.getContentResolver().registerContentObserver(Uri.parse(smsUri),true, smsInt = new SmsInterceptor(mHandler));
	 }
	 
	 

		public class CallInterceptor extends BroadcastReceiver{
			public CallInterceptor(){
			}
			@Override
			public void onReceive(Context c, Intent i){
				if (i.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)){
					String phoneNumber = (String) i.getExtras().get(Intent.EXTRA_PHONE_NUMBER);
					if (isBlocked(phoneNumber, blockedNums)){
						setResultData(null); //Don't make the call
						Toast.makeText(c, "Call blocked by SafeMode", Toast.LENGTH_SHORT).show();
						RedialAction ra = new RedialAction(phoneNumber);
						HistoryActivity.addItem(c, new BlockedCallItem(null, phoneNumber, ra));
					}
				}
			}
		}
		
		public class SmsInterceptor extends ContentObserver{
			private long startTime = System.currentTimeMillis(); //Don't delete messages sent before this
			
			public SmsInterceptor(Handler handler) {
				super(handler);
			}
			
			public void resetStartTime(){
				startTime = System.currentTimeMillis();
			}
			
			@Override
			public void onChange(boolean selfChange){
				try{
					Uri uriSMSURI = Uri.parse(smsUri);
					Cursor cur = mActivity.getContentResolver().query(uriSMSURI,
							new String[]{"address","_id","body"},
							"protocol IS NULL AND date > ?", new String[] {""+startTime}, null);
					while(cur.moveToNext()){
						String number = cur.getString(0);
						if (isBlocked(number, blockedNums)){
							String ID = cur.getString(1);
							String body = cur.getString(2);
							mActivity.getContentResolver().delete(uriSMSURI, "_id=?", new String[]{ID}); //Don't send it hopefully
							Toast.makeText(mActivity, "SMS blocked by SafeMode", Toast.LENGTH_SHORT).show();
							ViewTextAction vta = new ViewTextAction(number, body, SafeMode_BLOCKED_SMS_RESPONSE_CODE);
							ResendTextAction rta = new ResendTextAction(number, body, false);
							HistoryActivity.addItem(mActivity, new BlockedTextItem(null,number,vta,rta));
							break; //Don't delete all the messages that have been sent!
						}
					}
				} catch (Throwable t) {
					Log.w("SAFEMODE - SMS Blocking", "Failed to block text", t);
				}
			}
		}
		
		private boolean isBlocked(String number, Collection<String> blockedNums){
			for (String blocked : blockedNums){
				if (standardize(blocked).equals(standardize(number))) return true;
			}
			return false;
		}
		private String standardize(String number){
			String result = number.replaceAll("[\\s()+-]+", ""); //Strip whitespace, (, ), +, or -
			if (result.length() > 10) result = result.substring(result.length()-10); //Only get last 10 digits
			return result;
		}
}
