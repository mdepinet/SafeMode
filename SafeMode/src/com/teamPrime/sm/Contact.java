package com.teamPrime.sm;

import java.util.List;

public class Contact {
	
	private String iD;
	private String name;
	private Triple<String,Integer, String>[] numbers;
	
	public Contact (String iD, String name, List<Triple<String,Integer,String>> numbers){
		this.iD = iD;
		this.name = name;
		@SuppressWarnings("unchecked")
		Triple<String,Integer, String>[] nums = new Triple[numbers.size()];
		for (int i = 0; i<nums.length; i++){
			nums[i] = numbers.get(i);
		}
		this.numbers = nums;
	}
	
	public boolean match(String test){
		return test==this.name;
	}
	
	public String getID(){
		return iD;
	}
	
	public String getName(){
		return name;
	}
	
	public Triple<String,Integer,String>[] getNumber(){
		return numbers;
	}

}
