package com.teamPrime.sm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

public class ContactDAO {

	public static final String KEY_ID = "_id";
    public static final String KEY_EXT_ID = "ext_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_NUMS = "phone_nums";
    
    private static final String DATABASE_NAME = "SAFEMODE_data";
    private static final String DATABASE_TABLE = "blacklist_contacts";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "ContactDAO";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String TABLE_CREATE =
        "create table "+DATABASE_TABLE+" ("+KEY_ID+" bigint primary key autoincrement, "
        + KEY_EXT_ID + " bigint, "+ KEY_NAME + " text not null, "+KEY_NUMS+" varbinary);";

    
    private final Context mContext;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(TABLE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
            db.execSQL(TABLE_CREATE);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param context the Context within which to work
     */
    public ContactDAO(Context context) {
        this.mContext = context;
    }

    /**
     * Open the contacts database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ContactDAO open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new contact using a ContactDTO. If the contact is
     * successfully created return the new internal id for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param person The contact to add
     * @return internal id or -1 if failed
     * @throws IOException If the contact's phone numbers cannot be serialized
     */
    public long createNote(ContactDTO person) throws IOException {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_EXT_ID,person.getId());
        initialValues.put(KEY_NAME, person.getName());
        initialValues.put(KEY_NUMS, serialize(person.getPhoneNums()));

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the contact with the given internal id
     * 
     * @param id id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long id) {

        return mDb.delete(DATABASE_TABLE, KEY_ID + "=" + id, null) > 0;
    }

    /**
     * Return a Cursor over the list of all contacts in the database
     * 
     * @return Cursor over all contacts
     */
    public Cursor fetchAllBlacklistedContacts() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ID, KEY_EXT_ID, KEY_NAME,
                KEY_NUMS}, null, null, null, null, null);
    }
    
    /**
     * Return a Cursor positioned at the contact that matches the given internal id
     * 
     * @param id id of contact to retrieve
     * @return Cursor positioned to matching contact, if found
     * @throws SQLException if contact could not be found/retrieved
     */
    public Cursor fetchBlacklistedContact(long id) throws SQLException {

        Cursor mCursor =
            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ID,
                    KEY_EXT_ID, KEY_NAME, KEY_NUMS}, KEY_ID + "=" + id, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the contact using the details provided. The contact to be updated is
     * specified using the internal id, and it is altered to use the values passed in
     * 
     * @param id id of contact to update
     * @param ext_id value to set ext_id to
     * @param name value to set contact name to
     * @param phoneNums value to set contact phone numbers to
     * @return true if the note was successfully updated, false otherwise
     * @throws IOException If phoneNums cannot be serialized
     */
    public boolean updateNote(long id, long ext_id, String name, String[] phoneNums) throws IOException {
        ContentValues args = new ContentValues();
        args.put(KEY_EXT_ID, ext_id);
        args.put(KEY_NAME, name);
        args.put(KEY_NUMS, serialize(phoneNums));

        return mDb.update(DATABASE_TABLE, args, KEY_ID + "=" + id, null) > 0;
    }
    
    private byte[] serialize(Serializable obj) throws IOException{
    	if (obj == null) return null;
	      ByteArrayOutputStream baos = new ByteArrayOutputStream();
	      try {
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            try {
	                  oos.writeObject(obj);
	            } finally {
	                  oos.close();
	            }
	            return baos.toByteArray();
	      } catch (Exception x) {
	            throw new IOException ("Failed to convert Object to byte array.");
	      } finally {
	            try {baos.close();} catch (Exception x) {}
	      }
    }
    
    public Serializable deserialize(byte[] bytes) throws IOException{
    	if (bytes == null || bytes.length == 0) return null;
	      try {
	            ObjectInputStream clois = new ObjectInputStream(new ByteArrayInputStream(bytes));
	            Serializable anObject = (Serializable) clois.readObject();
	            return anObject;
	      } catch (Exception x) {
	            throw new IOException ("Failed to convert bytes to Object.");
	      }
    }
    
    
    
    public static int hideNumbers(Map<Long,Triple<String,Integer,String>[]> nums, ContentResolver cr){
    	int hidden = 0;
//    	ContentValues values = new ContentValues();
//    	values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, "555-555-5555");
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		int i = 0;
    	for (Map.Entry<Long, Triple<String,Integer,String>[]> pair : nums.entrySet()){
    		if (i >= 10){
    			try {
    	  	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
    	  	    }
    	  	     catch(Exception ex){
    	  	    	 Log.e("SAFEMODE",null, ex);
    	  	     }
    	  	     i = 0;
    		}
//    		hidden += cr.update(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, values,
//    				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ pair.getKey(), null);
    	    ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
    	            .withSelection(ContactsContract.Data.RAW_CONTACT_ID, new String[]{pair.getKey().toString()})
    	            .withSelection(ContactsContract.Data.MIMETYPE,new String[]{ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
    	            .withExpectedCount(pair.getValue().length).build());
    	    i++;
    	}
    	 try {
  	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
  	    }
  	     catch(Exception ex){
  	    	 Log.e("SAFEMODE",null, ex);
  	     }
    	return hidden;
    }
    
    public static int revealNumbers(Map<Long,Triple<String,Integer,String>[]> nums, ContentResolver cr){
    	int hidden = 0;
		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
    	for (Map.Entry<Long, Triple<String,Integer,String>[]> pair : nums.entrySet()){
    		for (Triple<String,Integer,String> trip : pair.getValue()){
//	    		ContentValues values = new ContentValues();
//	        	values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, trip.getFirst());
//	        	values.put(ContactsContract.CommonDataKinds.Phone.TYPE, trip.getSecond());
//	        	values.put(ContactsContract.CommonDataKinds.Phone.LABEL, trip.getThird());
//	        	String[] selectionArgs = { pair.getKey().toString(), trip.getSecond().toString(), trip.getThird()  };
//	    		hidden += cr.update(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, values,
//	    				ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?" +
//	    				" AND "+ContactsContract.CommonDataKinds.Phone.TYPE + " = ?" +
//	    				" AND ("+ContactsContract.CommonDataKinds.Phone.LABEL + "IS NULL OR " +
//	    				ContactsContract.CommonDataKinds.Phone.LABEL + " = ?"+")", selectionArgs);
        	    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        	            .withValue(ContactsContract.Data.RAW_CONTACT_ID, pair.getKey())
        	            .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        	            if (trip.getSecond() != null) builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE,trip.getSecond());
        	              else builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        	            builder = builder.withValue(ContactsContract.CommonDataKinds.Phone.LABEL,trip.getThird())
        	            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,trip.getFirst());
        	    ops.add(builder.build());
    		}
    		try {
     	        hidden += cr.applyBatch(ContactsContract.AUTHORITY, ops).length;
     	    }
     	     catch(Exception ex){
     	    	 Log.e("SAFEMODE",null, ex);
     	     }
    	}
    	return hidden;
    }
}