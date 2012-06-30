package com.teamPrime.sm.util;

import com.teamPrime.sm.util.MathUtils.MathUtilException;

public interface Solvable{
	String getHumanReadableEquation() throws MathUtilException;
	long getCorrectAnswer() throws MathUtilException;
}