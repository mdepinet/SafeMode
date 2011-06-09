package com.teamPrime.sm;

import java.util.List;

public class Contact {
	
	private String iD;
	private String name;
	private String[] numbers;
	
	public Contact (String iD, String name, List<String> numbers){
		this.iD = iD;
		this.name = name;
		String[] nums = new String[numbers.size()];
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
	
	public String[] getNumber(){
		return numbers;
	}

}
