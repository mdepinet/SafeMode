/**
 * Copyright � 2011 Mike Depinet
 * All rights reserved
 * 
 * This file is distributed as a part of the SAFEMODE application for
 * Android devices.  SAFEMODE is distributed under Apache License 2.0
 * which can be found in full at http://www.apache.org/licenses/LICENSE-2.0
 */

package com.teamPrime.sm.data;

import java.io.Serializable;

/**
 * ContactData holds data for a row from the Data table in
 * the contacts database.
 * 
 * @author Mike Depinet
 * @version 1.0
 *
 */
public interface ContactData extends Serializable{
	long getRawContactId();
	int isPrimary();
	int isSuperPrimary();
	int getDataVersion();
	String getMimeType();
	
	Object getData(int dataNum);
}

class ContactDataGeneric implements ContactData{
	private static final long serialVersionUID = -1521014230297655541L;
	private String mimeType;
	private long rawContactId;
	private int isPrimary;
	private int isSuperPrimary;
	private int dataVersion;
	private Object[] data;
	
	public ContactDataGeneric(String mimeType, long rawContactId, int isPrimary, int isSuperPrimary, int dataVersion){
		this.mimeType = mimeType;
		this.rawContactId = rawContactId;
		this.isPrimary = isPrimary;
		this.isSuperPrimary = isSuperPrimary;
		this.dataVersion = dataVersion;
	}
	public ContactDataGeneric(String mimeType, long rawContactId, int isPrimary, int isSuperPrimary, int dataVersion, Object... data){
		this(mimeType, rawContactId, isPrimary, isSuperPrimary, dataVersion);
		this.data = new Object[Math.min(15, data.length)]; //Throw out anything over 15 data columns since the table only has 15
		for (int i = 0; i<this.data.length; i++){
			this.data[i] = data[i];
		}
	}
	
	public String getMimeType(){
		return mimeType;
	}
	public long getRawContactId(){
		return rawContactId;
	}
	public int isPrimary(){
		return isPrimary;
	}
	public int isSuperPrimary(){
		return isSuperPrimary;
	}
	public int getDataVersion(){
		return dataVersion;
	}

	public Object getData(int dataNum){
		if (dataNum<1||dataNum>data.length) return null;
		else return data[dataNum-1];
	}

}