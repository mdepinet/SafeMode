package com.teamPrime.sm;

import java.util.Random;

public class MathUtils {
	
	private static final int[] smallChoice = {3, 4, 6, 7, 8, 9};
	private static final int[] largeChoice = {12, 13, 14, 16, 17, 18, 19};
	private static Random r = new Random();
	
	public static int genSmallRandom() {
		int index = r.nextInt(smallChoice.length);
		return smallChoice[index];
	}
	
	public static int genLargeRandom() {
		int index = r.nextInt(largeChoice.length);
		return largeChoice[index];
	}

}
