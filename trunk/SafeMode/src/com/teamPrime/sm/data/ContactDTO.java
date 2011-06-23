package com.teamPrime.sm.data;

import java.io.Serializable;

public class ContactDTO implements Serializable{
	private static final long serialVersionUID = 507225529375504029L;
	
	long id;
	String name;
	String[] phoneNums;
	
	public ContactDTO(long id, String name, String[] phoneNums){
		this.id = id;
		this.name = name;
		this.phoneNums = phoneNums;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getPhoneNums() {
		return phoneNums;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPhoneNums(String[] phoneNums) {
		this.phoneNums = phoneNums;
	}
}
