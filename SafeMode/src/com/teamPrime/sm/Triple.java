package com.teamPrime.sm;

public class Triple<K,V,L> {

	private K first;
	private V second;
	private L third;
	
	
	public Triple(K f, V s, L t) { 
	    first = f;
	    second = s; 
	    third = t;
	  }

	public K getFirst() {
	    return first;
	  }

	public V getSecond() {
	    return second;
	  }
	
	public L getThird() {
	    return third;
	  }

	public String toString() { 
	    return "(" + first.toString() + ", " + second.toString() + ", " + third.toString() + ")"; 
	  }

	}
	
