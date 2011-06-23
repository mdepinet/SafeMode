package com.teamPrime.sm.data;

import java.io.Serializable;

import android.provider.ContactsContract;

public interface ContactData extends Serializable{
	long getRawContactId();
	int isPrimary();
	int isSuperPrimary();
	int getDataVersion();
	
	String getMimeType();
	String getData1();
	int getData2();
	String getData3();
	String getProtocol();
	String getCustomProtocol();
}

abstract class ContactDataGeneric implements ContactData{
	private static final long serialVersionUID = -1521014230297655541L;
	private long rawContactId;
	private int isPrimary;
	private int isSuperPrimary;
	private int dataVersion;
	
	protected ContactDataGeneric(long rawContactId, int isPrimary, int isSuperPrimary, int dataVersion){
		this.rawContactId = rawContactId;
		this.isPrimary = isPrimary;
		this.isSuperPrimary = isSuperPrimary;
		this.dataVersion = dataVersion;
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
	
	public static ContactDataGeneric getData(String mimeType, long rawContactId, int isPrimary, int isSuperPrimary,
			int dataVersion, Object data1, Object data2, Object data3, Object data4, Object data5) throws DataCreationException{
		if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
			if (!(data1 instanceof String && data2 instanceof Integer && data3 instanceof String))
				throw new DataCreationException("Failed to create Phone data object due to data argument types: "
						+data1.getClass().getName()+", "+data2.getClass().getName()+", "+data3.getClass().getName());
			return new ContactDataPhone(rawContactId, isPrimary, isSuperPrimary, dataVersion, (String)data2, (Integer)data2, (String)data3);
		}
		else if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)){
			if (!(data1 instanceof String && data2 instanceof Integer && data3 instanceof String))
				throw new DataCreationException("Failed to create Email data object due to data argument types: "
						+data1.getClass().getName()+", "+data2.getClass().getName()+", "+data3.getClass().getName());
			return new ContactDataEmail(rawContactId, isPrimary, isSuperPrimary, dataVersion, (String)data2, (Integer)data2, (String)data3);
		}
		else if (mimeType.equals(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)){
			if (!(data1 instanceof String && data2 instanceof Integer && data3 instanceof String && data4 instanceof String && data5 instanceof String))
				throw new DataCreationException("Failed to create IM data object due to data argument types: "
						+data1.getClass().getName()+", "+data2.getClass().getName()+", "+data3.getClass().getName()
						+data4.getClass().getName()+", "+data5.getClass().getName());
			return new ContactDataIM(rawContactId, isPrimary, isSuperPrimary, dataVersion, (String)data2, (Integer)data2, (String)data3, (String)data4, (String)data5);
		}
		else{
			throw new DataCreationException("Failed to create data object due to unknown mimeType argument");
		}
	}
}

class ContactDataPhone extends ContactDataGeneric{
	private static final long serialVersionUID = 7900088598987939170L;
	private String number;
	private int type;
	private String label;
	
	public ContactDataPhone(long rawContactId, int isPrimary, int isSuperPrimary,
			int dataVersion, String number, int type, String label){
		super(rawContactId, isPrimary, isSuperPrimary, dataVersion);
		this.number = number;
		this.type = (type == 0 ? ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE : type);
		this.label = (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM ? label : "");
	}
	
	public String getMimeType(){
		return ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
	}
	public String getData1(){
		return number;
	}
	public int getData2(){
		return type;
	}
	public String getData3(){
		return label;
	}
	public String getProtocol(){
		return "";
	}
	public String getCustomProtocol(){
		return "";
	}
	public String getNumber(){
		return number;
	}
	public int getType(){
		return type;
	}
	public String getLabel(){
		return label;
	}
}

class ContactDataEmail extends ContactDataGeneric{
	private static final long serialVersionUID = 6567711053272212798L;
	private String address;
	private int type;
	private String label;
	
	public ContactDataEmail(long rawContactId, int isPrimary, int isSuperPrimary,
			int dataVersion, String address, int type, String label){
		super(rawContactId, isPrimary, isSuperPrimary, dataVersion);
		this.address = address;
		this.type = (type == 0 ? ContactsContract.CommonDataKinds.Email.TYPE_HOME : type);
		this.label = (type == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM ? label : "");
	}
	
	public String getMimeType(){
		return ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
	}
	public String getData1(){
		return address;
	}
	public int getData2(){
		return type;
	}
	public String getData3(){
		return label;
	}
	public String getProtocol(){
		return "";
	}
	public String getCustomProtocol(){
		return "";
	}
	public String getAddress(){
		return address;
	}
	public int getType(){
		return type;
	}
	public String getLabel(){
		return label;
	}
}

class ContactDataIM extends ContactDataGeneric{
	private static final long serialVersionUID = 5774383564726005101L;
	private String data;
	private int type;
	private String label;
	private String protocol;
	private String customProtocol;
	
	public ContactDataIM(long rawContactId, int isPrimary, int isSuperPrimary,
			int dataVersion, String data, int type, String label, String protocol, String customProtocol){
		super(rawContactId, isPrimary, isSuperPrimary, dataVersion);
		this.data = data;
		this.type = (type == 0 ? ContactsContract.CommonDataKinds.Email.TYPE_HOME : type);
		this.label = (type == ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM ? label : "");
		this.protocol = protocol;
		this.customProtocol = (protocol.equals(""+ContactsContract.CommonDataKinds.Im.TYPE_CUSTOM) ? customProtocol : "");
	}
	
	public String getMimeType(){
		return ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE;
	}
	public String getData1(){
		return data;
	}
	public int getData2(){
		return type;
	}
	public String getData3(){
		return label;
	}
	public String getProtocol(){
		return protocol;
	}
	public String getCustomProtocol(){
		return customProtocol;
	}
	public String getData(){
		return data;
	}
	public int getType(){
		return type;
	}
	public String getLabel(){
		return label;
	}
}

class DataCreationException extends Exception{
	private static final long serialVersionUID = -8226964422357218630L;
	public DataCreationException(){
		super();
	}
	public DataCreationException(String message){
		super(message);
	}
	public DataCreationException(String message, Throwable cause){
		super(message, cause);
	}
	public DataCreationException(Throwable cause){
		super(cause);
	}
}
