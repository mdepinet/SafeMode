/**
 * Copyright © 2012 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sml.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

/**
 * ContactDAO takes care of all contact data management.  All
 * insert, update, and delete SQL statements for the contacts
 * database come through here.
 * 
 * @author Mike Depinet
 * @version 2.0
 */
public class ContactDAO {
	private static final String contactSaveLoc = "SAFEMODE_contactData.bin";
	private static final String contactVMSaveLoc = "SAFEMODE_sendToVM.bin";
	private static final String tpcTempLoc1 = "SAFEMODE_twoPhaseCommitContacts.bin";
	private static final String tpcTempLoc2 = "SAFEMODE_twoPhaseCommitS2VM.bin";
	private static Map<Long, Integer> sendToVM = new HashMap<Long, Integer>(); //This will now be contact ids (not raw contact ids)
	private static final String supportedDataTypes = "(\'"+ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE+"\',"
														+"\'"+ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE+"\',"
														+"\'"+ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE+"\')";
	private static boolean revealNext = false;
	
	private static ContactDAO INSTANCE;
	private ContactDAO(){}
	private static ContactDAO getInstance(){
		return INSTANCE != null ? INSTANCE : (INSTANCE = new ContactDAO());
	}
    
    
    private static List<ContactData> getDataForContacts(ContentResolver cr, List<Long> contactIds){
    	List<ContactData> dataList = new LinkedList<ContactData>();
    	sendToVM.clear();
    	 Cursor rawCursor = cr.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID, RawContacts.CONTACT_ID},
    	          RawContacts.CONTACT_ID + " IN "+formatListForSQLin(contactIds), new String[]{}, null);
    	 if (rawCursor == null) return dataList;
    	 while (rawCursor.moveToNext()){
    		 if (!sendToVM.containsKey(rawCursor.getLong(1))){
	    		 Cursor contCursor = cr.query(Contacts.CONTENT_URI, new String[]{Contacts._ID, Contacts.SEND_TO_VOICEMAIL},
	    				 Contacts._ID+" =? ", new String[]{rawCursor.getString(1)}, null);
	    		 contCursor.moveToFirst();
	    		 sendToVM.put(contCursor.getLong(0), contCursor.getInt(1));
	    		 contCursor.close();
    		 }
    		 Cursor dataCursor =  cr.query(Data.CONTENT_URI,
    				 new String[] {Data.MIMETYPE, Data.RAW_CONTACT_ID, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY, Data.DATA_VERSION,
    				 	Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6, Data.CONTACT_ID},
    		         Data.RAW_CONTACT_ID + "=? AND "+Data.MIMETYPE + " in "+supportedDataTypes, new String[] {String.valueOf(rawCursor.getLong(0))}, null);
    		 while (dataCursor.moveToNext()){
				ContactData dataRow = new ContactDataGeneric(dataCursor.getString(0), dataCursor.getLong(11), dataCursor.getLong(1),
						 dataCursor.getInt(2), dataCursor.getInt(3), dataCursor.getInt(4),
						 getWithType(dataCursor,5), getWithType(dataCursor,6), getWithType(dataCursor,7),
						 getWithType(dataCursor,8), getWithType(dataCursor,9), getWithType(dataCursor,10));
				dataList.add(dataRow);
    		 }
    		 dataCursor.close();
    	 }
    	 rawCursor.close();
    	 return dataList;
    }
    private static int deleteData(ContentResolver cr, List<Long> contactIds){
    	int numDeleted = 0;
    	Cursor rawCursor = cr.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID},
  	          RawContacts.CONTACT_ID + " IN "+formatListForSQLin(contactIds), new String[]{}, null);
    	while (rawCursor.moveToNext()){
    		numDeleted += cr.delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=? AND "+Data.MIMETYPE+" IN "+supportedDataTypes,
    				new String[]{String.valueOf(rawCursor.getLong(0))});
    	}
    	rawCursor.close();
    	
    	//Send to voicemail
    	ContentValues cv = new ContentValues();
    	cv.put(Contacts.SEND_TO_VOICEMAIL, 1);
    	cr.update(Contacts.CONTENT_URI, cv, Contacts._ID + " IN "+formatListForSQLin(contactIds), new String[]{});
    	
    	return numDeleted;
    }
    private static int insertData(Context activity, List<ContactData> data){
    	if (data == null) return 0;
    	saveContacts(activity, data, tpcTempLoc1, tpcTempLoc2);
    	SharedPreferences.Editor edit = activity.getSharedPreferences("SAFEMODE", Context.MODE_PRIVATE).edit();
    	edit.putBoolean("writingContacts", true);
    	edit.commit();
    	ContentResolver cr = activity.getContentResolver();
    	
    	int numOps = 0;
    	boolean failedLast = false;
    	int numAttempts = 0;
    	while (!data.isEmpty() && numAttempts < 5){  //Keep trying until we get it right, but if it takes 5 attempts something is wrong.  Try again next time SafeMode starts.
    		Map<Long, Integer> s2vmLimited = limitS2VM(data);
	    	for (Map.Entry<Long, Integer> entry : s2vmLimited.entrySet()){
	    		ContentValues cv = new ContentValues();
	        	cv.put(Contacts.SEND_TO_VOICEMAIL, entry.getValue());
	        	cr.update(Contacts.CONTENT_URI, cv, Contacts._ID + " =?", new String[]{String.valueOf(entry.getKey())});
	    	}
	    	
	    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
	    	for (ContactData contact : data){
	    		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
	   	             .withValue(Data.RAW_CONTACT_ID, contact.getRawContactId())
	   	             .withValue(Data.MIMETYPE, contact.getMimeType())
	   	             .withValue(Data.DATA1, contact.getData(1))
	   	             .withValue(Data.DATA2, contact.getData(2))
	   	             .withValue(Data.DATA3, contact.getData(3))
	   	             .withValue(Data.DATA4, contact.getData(4))
	   	             .withValue(Data.DATA5, contact.getData(5))
	   	             .withValue(Data.DATA6, contact.getData(6))
	   	             .build());
	    	}
		    try {
				cr.applyBatch(ContactsContract.AUTHORITY, ops);
				numOps += ops.size();
				failedLast = false;
			} catch (RemoteException e) {
				Log.e("SAFEMODE - ContactDAO","Error inserting contacts",e);
				e.printStackTrace();
				if (failedLast) return numOps;
				else failedLast = true;
			} catch (OperationApplicationException e) {
				Log.e("SAFEMODE - ContactDAO","Error inserting contacts",e);
				e.printStackTrace();
				if (failedLast) return numOps;
				else failedLast = true;
			}
			
			List<Long> ids = new LinkedList<Long>();
			for (ContactData contact : data){
				ids.add(contact.getContactId());
			}
			List<ContactData> written = getDataForContacts(cr, ids);
			for (ContactData writtenContact : written){
				if (!data.contains(writtenContact)){
					Log.i("SAFEMODE - ContactDAO","Duplicate contact data.  Ignoring...");
				}
				else data.remove(writtenContact);
			}
			saveContacts(activity, data, tpcTempLoc1, tpcTempLoc2);
			numAttempts++;
    	}

    	edit = activity.getSharedPreferences("SAFEMODE", Context.MODE_PRIVATE).edit();
    	edit.putBoolean("writingContacts", false);
    	edit.commit();
    	
    	return numOps;
    }
    
    private static String formatListForSQLin(List<?> list){
    	if (list == null) return "()";
    	StringBuffer buff = new StringBuffer();
    	boolean nonEmpty = false;
    	buff.append("(");
    	for (Object obj : list){
    		if (obj != null) {
    			buff.append(obj.toString());
    			buff.append(", ");
    			nonEmpty = true;
    		}
    	}
    	if (nonEmpty){
	    	int index = buff.lastIndexOf(",");
	    	buff.replace(index,index+1,")");
    	}
    	else{
    		buff.append(")");
    	}
    	return buff.toString();
    }
    
    private static Object getWithType(Cursor c, int index){
    	//Turns out we only care whether or not it's a String because all numbers can be stored as Strings
    	try{
    		String s = c.getString(index);
    		if (s != null && !"".equals(s)) return s;
    	} catch (Exception ex){Log.v("SAFEMODE - DAO typing","It's not a String");}
    	
    	//Fine.  Deserialize it.
    	if (c.getBlob(index) == null) return null;
    	Object result = null;
    	ByteArrayInputStream bais = null;
    	ObjectInputStream ois = null;
    	try {
    		bais = new ByteArrayInputStream(c.getBlob(index));
			ois = new ObjectInputStream(bais);
			result = ois.readObject();
		} catch (StreamCorruptedException e) {
			Log.e("SAFEMODE - ContactDAO", "Failed to deserialize blob", e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("SAFEMODE - ContactDAO", "Failed to deserialize blob", e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.e("SAFEMODE - ContactDAO", "Failed to deserialize blob", e);
			e.printStackTrace();
		} finally {
			try{bais.close();ois.close();} catch(Throwable t){}
		}
		return result;
    }
    
    private static void saveContacts(Context activity, List<ContactData> contacts, String contactsSaveLoc, String s2vmSaveLoc){
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = null;
    	FileOutputStream fos = null;
    	try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(contacts);
			fos = activity.openFileOutput(contactsSaveLoc, Activity.MODE_PRIVATE);
			fos.write(baos.toByteArray());
		} catch (IOException e) {
			Log.e("SAFEMODE","Error writing contact list",e);
			e.printStackTrace();
		} finally{
			try{fos.close();oos.close();} catch(Throwable t){}
		}
		
		baos = new ByteArrayOutputStream();
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(limitS2VM(contacts));
			fos = activity.openFileOutput(s2vmSaveLoc, Activity.MODE_PRIVATE);
			fos.write(baos.toByteArray());
		} catch (IOException e) {
			Log.e("SAFEMODE","Error writing contact list",e);
			e.printStackTrace();
		} finally{
			try{fos.close();oos.close();} catch(Throwable t){}
		}
    }
    
    private static Map<Long, Integer> limitS2VM(List<ContactData> contacts){
    	Map<Long, Integer> s2vmLimited = new HashMap<Long, Integer>();
    	if (contacts == null) return s2vmLimited;
		for (Map.Entry<Long, Integer> entry : sendToVM.entrySet()){
			for (ContactData contact : contacts){
				if (entry.getKey() == contact.getContactId()) s2vmLimited.put(entry.getKey(), entry.getValue());
			}
		}
		return s2vmLimited;
    }
    
    @SuppressWarnings("unchecked")
	private static List<ContactData> readContacts(Context activity, String saveLoc){
    	List<ContactData> result = null;
    	ObjectInputStream ois = null;
    	try {
			ois = new ObjectInputStream(activity.openFileInput(saveLoc));
			result = (List<ContactData>) ois.readObject();
		} catch (StreamCorruptedException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} finally{
			try{ois.close();} catch(Throwable t){}
		}
		try {
			ois = new ObjectInputStream(activity.openFileInput(contactVMSaveLoc));
			sendToVM = (Map<Long, Integer>) ois.readObject();
		} catch (StreamCorruptedException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Log.e("SAFEMODE","Error reading contact list",e);
			e.printStackTrace();
		} finally{
			try{ois.close();} catch(Throwable t){}
		}
		return result;
    }
    
    private synchronized List<ContactData> hideContactsInternal(Context activity, List<Long> contactIds){
    	if (revealNext) revealContactsInternal(activity, contactSaveLoc); //Must reveal before hiding again
    	List<ContactData> dataList = getDataForContacts(activity.getContentResolver(), contactIds);
    	saveContacts(activity, dataList, contactSaveLoc, contactVMSaveLoc);
    	deleteData(activity.getContentResolver(), contactIds);
    	revealNext = true;
    	return dataList;
    }
    private synchronized int revealContactsInternal(Context activity,  String saveLoc){
    	List<ContactData> dataList = readContacts(activity, saveLoc);
    	int result = insertData(activity, dataList);
    	revealNext = false;
    	return result;
    }
    
    public synchronized static List<ContactData> hideContacts(Context activity, List<Long> contactIds){
    	return getInstance().hideContactsInternal(activity, contactIds);
    }
    public synchronized static int revealContacts(Context activity){
    	return getInstance().revealContactsInternal(activity, contactSaveLoc);
    }
    public synchronized static int continueReveal(Context activity){
    	return getInstance().revealContactsInternal(activity, tpcTempLoc1);
    }
    
    public static Collection<String> getPhoneNumbersForContacts(Collection<ContactData> allData){
    	Collection<String> numbers = new TreeSet<String>();
    	for (ContactData data : allData){
    		if (data.getMimeType().equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
    			numbers.add((String) data.getData(1));
    		}
    	}
    	return numbers;
    }
}