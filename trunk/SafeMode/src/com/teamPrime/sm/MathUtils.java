package com.teamPrime.sm;

import java.util.Random;

public class MathUtils {
	
	private static final int[] smallChoice = {3, 4, 6, 7, 8, 9};
	
	public static int genSmallRandom() {
		Random r = new Random();
		int ind = r.nextInt(6);
		return smallChoice[ind];
	}
	
	public static int genLargeRandom() {
		Random r = new Random();
		int ran = r.nextInt(8);
	    ran += 12;
		return ran;
	}

}
