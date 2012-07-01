package com.teamPrime.sml.util;

import com.teamPrime.sml.util.MathUtils.MathUtilException;

public interface Solvable{
	String getHumanReadableEquation() throws MathUtilException;
	long getCorrectAnswer() throws MathUtilException;
}