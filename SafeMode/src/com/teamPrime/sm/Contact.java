package com.teamPrime.sm;

public class Contact {
	
	private String iD;
	private String name;
	private String number;
	
	public Contact (String iD, String name, String number){
		this.iD = iD;
		this.name = name;
		this.number = number;
	}
	
	public String getID(){
		return iD;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNumber(){
		return number;
	}

}
