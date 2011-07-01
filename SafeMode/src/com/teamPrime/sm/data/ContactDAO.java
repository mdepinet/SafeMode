package com.teamPrime.sm.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class ContactDAO {
	private static final String contactSaveLoc = "SAFEMODE_contactData.bin";
	private static boolean lastHid = false;
	private static final String supportedDataTypes = "(\'"+ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE+"\',"
														+"\'"+ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE+"\',"
														+"\'"+ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE+"\')";
    
    
    private static List<ContactData> getDataForContacts(ContentResolver cr, List<Long> contactIds){
    	List<ContactData> dataList = new LinkedList<ContactData>();
    	 Cursor rawCursor = cr.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID},
    	          RawContacts.CONTACT_ID + " IN "+formatListForSQLin(contactIds), new String[]{}, null);
    	 while (rawCursor.moveToNext()){
    		 Cursor dataCursor =  cr.query(Data.CONTENT_URI,
    				 new String[] {Data.MIMETYPE, Data.RAW_CONTACT_ID, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY, Data.DATA_VERSION,
    				 	Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6},
    		         Data.RAW_CONTACT_ID + "=? AND "+Data.MIMETYPE + " in "+supportedDataTypes, new String[] {String.valueOf(rawCursor.getLong(0))}, null);
    		 while (dataCursor.moveToNext()){
    			 Log.i("SAFEMODE - ContactDAO", "Mimetype is "+dataCursor.getString(0));
    			 try {
					ContactData dataRow = ContactDataGeneric.getData(dataCursor.getString(0), dataCursor.getLong(1),
							 dataCursor.getInt(2), dataCursor.getInt(3), dataCursor.getInt(4), deserializeBlob(dataCursor.getBlob(5)),
							 deserializeBlob(dataCursor.getBlob(6)), deserializeBlob(dataCursor.getBlob(7)),
							 deserializeBlob(dataCursor.getBlob(8)), deserializeBlob(dataCursor.getBlob(9)));
					dataList.add(dataRow);
				} catch (DataCreationException e) {
					Log.e("SAFEMODE - ContactDAO", "Failed to create data object", e);
					e.printStackTrace();
				}
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
    	return numDeleted;
    }
    private static int insertData(ContentResolver cr, List<ContactData> data){
    	ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	for (ContactData contact : data){
    		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
   	             .withValue(Data.RAW_CONTACT_ID, contact.getRawContactId())
   	             .withValue(Data.MIMETYPE, contact.getMimeType())
   	             .withValue(Data.DATA1, contact.getData1())
   	             .withValue(Data.DATA2, contact.getData2())
   	             .withValue(Data.DATA3, contact.getData3())
   	             .withValue(Data.DATA4, contact.getProtocol())
   	             .withValue(Data.DATA5, contact.getCustomProtocol())
   	             .build());
    	}
	    try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
			return ops.size();
		} catch (RemoteException e) {
			Log.e("SAFEMODE - ContactDAO","Error inserting contacts",e);
			e.printStackTrace();
			return 0;
		} catch (OperationApplicationException e) {
			Log.e("SAFEMODE - ContactDAO","Error inserting contacts",e);
			e.printStackTrace();
			return 0;
		}

    }
    
    private static String formatListForSQLin(List<?> list){
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
    private static Object deserializeBlob(byte[] blob){
    	if (blob == null) return blob;
    	if (blob[blob.length-1] == 0){
	    	String strAttempt = new String(blob,0,blob.length-1);
	    	if (strAttempt.matches("[\\w\\s)(-]*")){ //This really is a String!
	    		if (strAttempt.matches("\\d+")){ //But it could also be a long
	    			//Return the most specific type possible
    				byte b = Byte.parseByte(strAttempt);
    				short s = Short.parseShort(strAttempt);
    				int i = Integer.parseInt(strAttempt);
    				long l = Long.parseLong(strAttempt);
	    			if (b == l) return new Byte(b);
	    			else if (s == l) return new Short(s);
	    			else if (i == l) return new Integer(i);
	    			else return l;
	    		}
	    		else if (strAttempt.matches("\\d*[.]\\d+")){ //Or a double
	    			//Return the most specific type possible
	    			float f = Float.parseFloat(strAttempt);
	    			double d = Double.parseDouble(strAttempt);
	    			if (f == d) return new Float(f);
	    			else return new Double(d);
	    		}
	    		else return strAttempt;
	    	}
    	}
    	Object result = null;
    	ByteArrayInputStream bais = null;
    	ObjectInputStream ois = null;
    	try {
    		bais = new ByteArrayInputStream(blob);
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
    
    private static void saveContacts(Activity activity, List<ContactData> contacts){
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
    	ObjectOutputStream oos = null;
    	FileOutputStream fos = null;
    	try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(contacts);
			fos = activity.openFileOutput(contactSaveLoc, Activity.MODE_PRIVATE);
			fos.write(baos.toByteArray());
		} catch (IOException e) {
			Log.e("SAFEMODE","Error writing contact list",e);
			e.printStackTrace();
		} finally{
			try{fos.close();oos.close();} catch(Throwable t){}
		}
    }
    
    @SuppressWarnings("unchecked")
	private static List<ContactData> readContacts(Activity activity){
    	List<ContactData> result = null;
    	ObjectInputStream ois = null;
    	try {
			ois = new ObjectInputStream(activity.openFileInput(contactSaveLoc));
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
		return result;
    }
    
    public static int hideContacts(Activity activity, List<Long> contactIds) throws DataAccessException{
    	if (lastHid) throw new DataAccessException("Last batch of contacts was not revealed.  Cannot hide more.");
    	List<ContactData> dataList = getDataForContacts(activity.getContentResolver(), contactIds);
    	saveContacts(activity, dataList);
    	deleteData(activity.getContentResolver(), contactIds);
    	return dataList.size();
    }
    public static int revealContacts(Activity activity){
    	List<ContactData> dataList = readContacts(activity);
    	return insertData(activity.getContentResolver(), dataList);
    }
    
    
    public static class DataAccessException extends Exception{
    	private static final long serialVersionUID = 7824516446656553631L;
    	public DataAccessException(){
    		super();
    	}
    	public DataAccessException(String message){
    		super(message);
    	}
    	public DataAccessException(String message, Throwable cause){
    		super(message, cause);
    	}
    	public DataAccessException(Throwable cause){
    		super(cause);
    	}
    }
}