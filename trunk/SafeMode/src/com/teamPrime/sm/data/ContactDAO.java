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
//	public static final String KEY_ID = "_id";
//    public static final String KEY_EXT_ID = "ext_id";
//    public static final String KEY_NAME = "name";
//    public static final String KEY_NUMS = "phone_nums";
//    
//    private static final String DATABASE_NAME = "SAFEMODE_data";
//    private static final String DATABASE_TABLE = "blacklist_contacts";
//    private static final int DATABASE_VERSION = 1;
//
//    private static final String TAG = "ContactDAO";
//    private DatabaseHelper mDbHelper;
//    private SQLiteDatabase mDb;
//
//    /**
//     * Database creation sql statement
//     */
//    private static final String TABLE_CREATE =
//        "create table "+DATABASE_TABLE+" ("+KEY_ID+" bigint primary key autoincrement, "
//        + KEY_EXT_ID + " bigint, "+ KEY_NAME + " text not null, "+KEY_NUMS+" varbinary);";
//
//    
//    private final Context mContext;
//
//    private static class DatabaseHelper extends SQLiteOpenHelper {
//
//        DatabaseHelper(Context context) {
//            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        }
//
//        @Override
//        public void onCreate(SQLiteDatabase db) {
//
//            db.execSQL(TABLE_CREATE);
//        }
//
//        @Override
//        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
//                    + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
//            db.execSQL(TABLE_CREATE);
//        }
//    }
//
//    /**
//     * Constructor - takes the context to allow the database to be
//     * opened/created
//     * 
//     * @param context the Context within which to work
//     */
//    public ContactDAO(Context context) {
//        this.mContext = context;
//    }
//
//    /**
//     * Open the contacts database. If it cannot be opened, try to create a new
//     * instance of the database. If it cannot be created, throw an exception to
//     * signal the failure
//     * 
//     * @return this (self reference, allowing this to be chained in an
//     *         initialization call)
//     * @throws SQLException if the database could be neither opened or created
//     */
//    public ContactDAO open() throws SQLException {
//        mDbHelper = new DatabaseHelper(mContext);
//        mDb = mDbHelper.getWritableDatabase();
//        return this;
//    }
//
//    public void close() {
//        mDbHelper.close();
//    }
//
//
//    /**
//     * Create a new contact using a ContactDTO. If the contact is
//     * successfully created return the new internal id for that note, otherwise return
//     * a -1 to indicate failure.
//     * 
//     * @param person The contact to add
//     * @return internal id or -1 if failed
//     * @throws IOException If the contact's phone numbers cannot be serialized
//     */
//    public long createNote(ContactDTO person) throws IOException {
//        ContentValues initialValues = new ContentValues();
//        initialValues.put(KEY_EXT_ID,person.getId());
//        initialValues.put(KEY_NAME, person.getName());
//        initialValues.put(KEY_NUMS, serialize(person.getPhoneNums()));
//
//        return mDb.insert(DATABASE_TABLE, null, initialValues);
//    }
//
//    /**
//     * Delete the contact with the given internal id
//     * 
//     * @param id id of note to delete
//     * @return true if deleted, false otherwise
//     */
//    public boolean deleteNote(long id) {
//
//        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
//    }
//
//    /**
//     * Return a Cursor over the list of all contacts in the database
//     * 
//     * @return Cursor over all contacts
//     */
//    public Cursor fetchAllBlacklistedContacts() {
//
//        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_EXT_ID, KEY_NAME,
//                KEY_NUMS}, null, null, null, null, null);
//    }
//    
//    /**
//     * Return a Cursor positioned at the contact that matches the given internal id
//     * 
//     * @param id id of contact to retrieve
//     * @return Cursor positioned to matching contact, if found
//     * @throws SQLException if contact could not be found/retrieved
//     */
//    public Cursor fetchBlacklistedContact(long id) throws SQLException {
//
//        Cursor mCursor =
//            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID,
//                    KEY_EXT_ID, KEY_NAME, KEY_NUMS}, KEY_ID + "=" + id, null,
//                    null, null, null, null);
//        if (mCursor != null) {
//            mCursor.moveToFirst();
//        }
//        return mCursor;
//
//    }
//
//    /**
//     * Update the contact using the details provided. The contact to be updated is
//     * specified using the internal id, and it is altered to use the values passed in
//     * 
//     * @param id id of contact to update
//     * @param ext_id value to set ext_id to
//     * @param name value to set contact name to
//     * @param phoneNums value to set contact phone numbers to
//     * @return true if the note was successfully updated, false otherwise
//     * @throws IOException If phoneNums cannot be serialized
//     */
//    public boolean updateContact(long id, long ext_id, String name, String[] phoneNums) throws IOException {
//        ContentValues args = new ContentValues();
//        args.put(KEY_EXT_ID, ext_id);
//        args.put(KEY_NAME, name);
//        args.put(KEY_NUMS, serialize(phoneNums));
//
//        return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + id, null) > 0;
//    }
//    
//    private byte[] serialize(Serializable obj) throws IOException{
//    	if (obj == null) return null;
//	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	      try {
//	            ObjectOutputStream oos = new ObjectOutputStream(baos);
//	            try {
//	                  oos.writeObject(obj);
//	            } finally {
//	                  oos.close();
//	            }
//	            return baos.toByteArray();
//	      } catch (Exception x) {
//	            throw new IOException ("Failed to convert Object to byte array.");
//	      } finally {
//	            try {baos.close();} catch (Exception x) {}
//	      }
//    }
//    
//    public Serializable deserialize(byte[] bytes) throws IOException{
//    	if (bytes == null || bytes.length == 0) return null;
//	      try {
//	            ObjectInputStream clois = new ObjectInputStream(new ByteArrayInputStream(bytes));
//	            Serializable anObject = (Serializable) clois.readObject();
//	            return anObject;
//	      } catch (Exception x) {
//	            throw new IOException ("Failed to convert bytes to Object.");
//	      }
//    }
    
//    
//    
//    public static int hideNumbers(Map<Long,Triple<String,Integer,String>[]> nums, ContentResolver cr){
//    	int hidden = 0;
////    	ContentValues values = new ContentValues();
////    	values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, "555-555-5555");
//		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//		int i = 0;
//    	for (Map.Entry<Long, Triple<String,Integer,String>[]> pair : nums.entrySet()){
//    		if (i >= 10){
//    			try {
//    	  	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
//    	  	    }
//    	  	     catch(Exception ex){
//    	  	    	 Log.e("SAFEMODE",null, ex);
//    	  	     }
//    	  	     i = 0;
//    		}
////    		hidden += cr.update(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, values,
////    				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ pair.getKey(), null);
//    	    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
//    	            .withSelection(ContactsContract.Data.RAW_CONTACT_ID, new String[]{pair.getKey().toString()})
//    	            .withSelection(ContactsContract.Data.MIMETYPE,new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
//    	            .withExpectedCount(pair.getValue().length).build());
//    	    i++;
//    	}
//    	 try {
//  	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
//  	    }
//  	     catch(Exception ex){
//  	    	 Log.e("SAFEMODE",null, ex);
//  	     }
//    	return hidden;
//    }
//    
//    public static int revealNumbers(Map<Long,Triple<String,Integer,String>[]> nums, ContentResolver cr){
//    	int hidden = 0;
//		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//    	for (Map.Entry<Long, Triple<String,Integer,String>[]> pair : nums.entrySet()){
//    		for (Triple<String,Integer,String> trip : pair.getValue()){
////	    		ContentValues values = new ContentValues();
////	        	values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, trip.getFirst());
////	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, trip.getSecond());
////	        	values.put(ContactsContract.CommonDataKinds.Phone.LABEL, trip.getThird());
////	        	String[] selectionArgs = { pair.getKey().toString(), trip.getSecond().toString(), trip.getThird()  };
////	    		hidden += cr.update(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, values,
////	    				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?" +
////	    				" AND "+ContactsContract.CommonDataKinds.Phone.TYPE + " = ?" +
////	    				" AND ("+ContactsContract.CommonDataKinds.Phone.LABEL + "IS NULL OR " +
////	    				ContactsContract.CommonDataKinds.Phone.LABEL + " = ?"+")", selectionArgs);
//        	    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//        	            .withValue(ContactsContract.Data.RAW_CONTACT_ID, pair.getKey())
//        	            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
//        	            if (trip.getSecond() != null) builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,trip.getSecond());
//        	              else builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
//        	            builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.LABEL,trip.getThird())
//        	            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,trip.getFirst());
//        	    ops.add(builder.build());
//    		}
//    		try {
//     	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
//     	    }
//     	     catch(Exception ex){
//     	    	 Log.e("SAFEMODE",null, ex);
//     	     }
//    	}
//    	return hidden;
//    }
    
    
	private static final String contactSaveLoc = "SAFEMODE_contactData.bin";
	private static boolean lastHid = false;
    
    
    private static List<ContactData> getDataForContacts(ContentResolver cr, List<Long> contactIds){
    	List<ContactData> dataList = new LinkedList<ContactData>();
    	 Cursor rawCursor = cr.query(RawContacts.CONTENT_URI, new String[]{RawContacts._ID},
    	          RawContacts.CONTACT_ID + " IN ?", new String[]{formatListForSQLin(contactIds)}, null);
    	 while (rawCursor.moveToNext()){
    		 Cursor dataCursor =  cr.query(Data.CONTENT_URI,
    				 new String[] {Data.MIMETYPE, Data.RAW_CONTACT_ID, Data.IS_PRIMARY, Data.IS_SUPER_PRIMARY, Data.DATA_VERSION,
    				 	Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6},
    		         Data.RAW_CONTACT_ID + "=?", new String[] {String.valueOf(rawCursor.getLong(0))}, null);
    		 while (dataCursor.moveToNext()){
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
  	          RawContacts.CONTACT_ID + " IN ?", new String[]{formatListForSQLin(contactIds)}, null);
    	while (rawCursor.moveToNext()){
    		numDeleted += cr.delete(Data.CONTENT_URI, Data.RAW_CONTACT_ID + "=?", new String[]{String.valueOf(rawCursor.getLong(0))});
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
    	buff.append("(");
    	for (Object obj : list){
    		buff.append(obj.toString());
    		buff.append(", ");
    	}
    	if (!list.isEmpty()){
	    	int index = buff.lastIndexOf(",");
	    	buff.replace(index,index+1,")");
    	}
    	else{
    		buff.append(")");
    	}
    	return buff.toString();
    }
    private static Object deserializeBlob(byte[] blob){
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