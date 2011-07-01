package com.teamPrime.sm.tasks;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.teamPrime.sm.data.ContactDAO;
import com.teamPrime.sm.data.ContactDAO.DataAccessException;

public class BlackListIOTask extends AsyncTask<Void, Void, List<Long>> {
	public static final int WRITE_IDS_MODE = 0;
	public static final int READ_IDS_MODE = 1;
	public static final int HIDE_CONTACTS_MODE = 2; //Note that this must also read ids
	public static final int REVEAL_CONTACTS_MODE = 3;
	private static final String fileName = "SAFEMODE_contactIds.bin";
	
	private Activity mActivity;
	private int mode;
	private List<Long> contactIds;
	
	public BlackListIOTask(Activity activity, List<Long> contactIds, int mode){
		mActivity = activity;
		this.contactIds = contactIds;
		this.mode = mode;
	}
	public void setActivity(Activity activity){
		mActivity = activity;
	}
	
	 //Runs on main thread.
	 @Override
	 protected void onPreExecute() {
	 }

  // Runs on main thread.
	 @Override
	 protected void onProgressUpdate(Void... arg0) {
	 }

	 // Runs on main thread.
	 @Override
	 protected void onPostExecute(List<Long> result) {
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
			 try {
				 i = ContactDAO.hideContacts(mActivity, contactIds);
			 } catch (DataAccessException e) {
				 Log.e("SAFEMODE - ContactsIO", "Failed to hide contacts",e);
			 }
			 List<Long> result = new LinkedList<Long>();
			 result.add((long) i);
			 return result;
		 case REVEAL_CONTACTS_MODE:
			 int j = ContactDAO.revealContacts(mActivity);
			 List<Long> result2 = new LinkedList<Long>();
			 result2.add((long) j);
			 return result2;
		 }
		 return null;
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
}
//
//public class BlackListIOTask extends AsyncTask<Void, Void, Void> {
//	private Activity mActivity;
//	private Map<Long, Triple<String,Integer,String>[]> blackMap;
//	private Map<Long, Triple<String,Integer,String>[]> fullMap;
//	private int updateDB;
//	
//	private static final String BL_FILENAME = "Blacklist.bin";
//	private static final String FULL_FILENAME = "Contacts.bin";
//
//    public BlackListIOTask(Activity activity, Map<Long, Triple<String,Integer,String>[]> fullMap, Map<Long, Triple<String,Integer,String>[]> blackMap, int updateDB) {
//        mActivity = activity;
//        this.blackMap = blackMap;
//        this.fullMap = fullMap;
//        this.updateDB = updateDB;
//    }
//
//    // Can change activity this task points to.
//    // e.g. when activity recreated after orientation change.
//    public void setActivity(Activity activity) {
//        mActivity = activity;
//    }
//
//    // Runs on main thread.
//    @Override
//    protected void onPreExecute() {
//    }
//
//    // Runs on main thread.
//    @Override
//    protected void onProgressUpdate(Void... arg0) {
//    }
//
//    // Runs on main thread.
//    @Override
//    protected void onPostExecute(Void result) {
//    }
//	
//	@SuppressWarnings("unchecked")
//	@Override
//	protected Void doInBackground(Void... arg0) {
//		if (blackMap != null || fullMap != null){ //Write mode
//			ObjectOutputStream oos = null;
//			if (blackMap != null){
//				try {
//					oos = new ObjectOutputStream(mActivity.openFileOutput(BL_FILENAME, Context.MODE_PRIVATE));
//					oos.writeObject(blackMap);
//				} catch (FileNotFoundException e) {
//					Log.e("SAFEMODE", null, e);
//				} catch (IOException e) {
//					Log.e("SAFEMODE", null, e);
//				}
//				finally{
//					try{oos.close();}catch(Throwable t){}
//				}
//			}
//			if (fullMap != null){
//				try {
//					oos = new ObjectOutputStream(mActivity.openFileOutput(FULL_FILENAME, Context.MODE_PRIVATE));
//					oos.writeObject(fullMap);
//				} catch (FileNotFoundException e) {
//					Log.e("SAFEMODE", null, e);
//				} catch (IOException e) {
//					Log.e("SAFEMODE", null, e);
//				}
//				finally{
//					try{oos.close();}catch(Throwable t){}
//				}
//			}
//		}
//		else{ //Read mode
//			ObjectInputStream ois = null;
//			if (blackMap == null){
//				try{
//					ois = new ObjectInputStream(mActivity.openFileInput(BL_FILENAME));
//					blackMap = (Map<Long, Triple<String,Integer,String>[]>) ois.readObject();
//				} catch(IOException e) {
//					Log.e("SAFEMODE", null, e);
//				} catch (ClassNotFoundException e) {
//					Log.e("SAFEMODE", null, e);
//				}
//				finally{
//					try{ois.close();}catch(Throwable t){}
//				}
//			}
//			if (fullMap == null){
//				try{
//					ois = new ObjectInputStream(mActivity.openFileInput(FULL_FILENAME));
//					fullMap = (Map<Long, Triple<String,Integer,String>[]>) ois.readObject();
//				} catch(IOException e) {
//					Log.e("SAFEMODE", null, e);
//				} catch (ClassNotFoundException e) {
//					Log.e("SAFEMODE", null, e);
//				}
//				finally{
//					try{ois.close();}catch(Throwable t){}
//				}
//			}
//			
//			switch (updateDB){
//			case 0:
//				return null;
//			case 1:
//				ContactDAO.revealNumbers(fullMap, mActivity.getContentResolver());
//				ContactDAO.hideNumbers(blackMap, mActivity.getContentResolver());
//				return null;
//			case 2:
//				ContactDAO.revealNumbers(blackMap, mActivity.getContentResolver());
//				ContactDAO.hideNumbers(fullMap, mActivity.getContentResolver());
//				return null;
//			case 3:
//				ContactDAO.revealNumbers(fullMap, mActivity.getContentResolver());
//				return null;
//			case 4:
//				ContactDAO.revealNumbers(blackMap, mActivity.getContentResolver());
//				return null;
//			default:
//				return null;
//			}
//		}
//		return null;
//	}
//	
//}
