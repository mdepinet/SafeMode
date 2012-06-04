package com.teamPrime.sm.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

public class MathUtils {
	public enum Operator{
		ADDITION("+", new Applyable(){
			public long apply(long left, long right){
				return left + right;
			}
		}),
		MULTIPLICATION("*", new Applyable(){
			public long apply(long left, long right){
				return left * right;
			}
		}),
		SUBTRACTION("-", new Applyable(){
			public long apply(long left, long right){
				return left - right;
			}
		});
		
		private String symbol;
		private Applyable fn;
		Operator(String symbol, Applyable fn){
			this.symbol = symbol;
			this.fn = fn;
		}
		public String getSymbol(){
			return symbol;
		}
		public long apply(long left, long right){
			return fn.apply(left, right);
		}
		public static List<String> getSymbolList(){
			List<String> symbols = new ArrayList<String>();
			for (Operator o : values()){
				symbols.add(o.getSymbol());
			}
			return symbols;
		}
		public static Operator getOpFromSymbol(String symbol){
			for (Operator o : values()){
				if (o.getSymbol().equals(symbol)) return o;
			}
			return null;
		}
		
		interface Applyable{
			public long apply(long left, long right);
		}
	}
	
	private static final int[] digitChoices = {3, 4, 6, 7, 8, 9};
	private static Random r = new Random();

	private static int getRandomNDigitInt(int n){
		return getRandomNDigitInt(n, false);
	}
	
	private static int getRandomNDigitInt(int n, boolean useAllDigits){
		int result = 0;
		for (int i = 0; i < n; i++){
			int digit = useAllDigits ? r.nextInt(10) : digitChoices[r.nextInt(digitChoices.length)];
			result = result * 10 + digit;
		}
		return result;
	}
	
	public static Solvable generateProblem(List<Operator> operators, int maxNumDigits, int maxDepth){
		maxDepth = Math.max(maxDepth, 2); //Must have at least two levels
		OperationTree root = new OperationTree(operators.get(r.nextInt(operators.size())).getSymbol());
		Queue<OperationTree> unfilledOperators1 = new LinkedList<OperationTree>();
		Queue<OperationTree> unfilledOperators2 = new LinkedList<OperationTree>();
		unfilledOperators1.add(root);
		for (int level = 2; level <= maxDepth; level++){ //1 is root
			while (!unfilledOperators1.isEmpty()){
				OperationTree next = unfilledOperators1.remove();
				boolean makeOperator = r.nextFloat() < (float)(maxDepth-level)/(float)(maxDepth);
				if (makeOperator){
					next.left = new OperationTree(operators.get(r.nextInt(operators.size())).getSymbol());
					unfilledOperators2.add(next.left);
				} else {
					next.left = new OperationTree(Integer.toString(getRandomNDigitInt((int)(r.nextDouble()*maxNumDigits)+1)));
				}
				makeOperator = r.nextFloat() < (float)(maxDepth-level)/(float)(maxDepth);
				if (makeOperator){
					next.right = new OperationTree(operators.get(r.nextInt(operators.size())).getSymbol());
					unfilledOperators2.add(next.right);
				} else {
					next.right = new OperationTree(Integer.toString(getRandomNDigitInt(Math.min(maxNumDigits, level))));
				}
			}
			Queue<OperationTree> temp = unfilledOperators1;
			unfilledOperators1 = unfilledOperators2;
			unfilledOperators2 = temp;
		}
		assert(unfilledOperators1.isEmpty());
		assert(unfilledOperators2.isEmpty());
		return root;
	}

	private static class OperationTree implements Solvable{
    	private String value; //Either an operator or an integer
    	OperationTree left;
    	OperationTree right;
    	
    	OperationTree(String value){
    		this.value = value;
    	}
    	
    	long evaluate() throws MathUtilException{
    		if (value == null){
    			throw new MathUtilException("Encountered null value");
    		}
    		if (value.matches("[\\d]+")){
    			return Integer.valueOf(value);
    		}
    		else if (Operator.getSymbolList().contains(value)){
    			return Operator.getOpFromSymbol(value).apply(left.evaluate(), right.evaluate());
    		}
    		else {
    			throw new MathUtilException("Unknown operator "+value);
    		}
    	}
    	
    	String getHumanReadable() throws MathUtilException{
    		if (value == null){
    			throw new MathUtilException("Encountered null value");
    		}
    		if (value.matches("[\\d]+")){
    			return value;
    		}
    		else if (Operator.getSymbolList().contains(value)){
    			return "(" + left.getHumanReadable() + " " + value + " " + right.getHumanReadable() + ")";
    		}
    		else {
    			throw new MathUtilException("Unknown operator "+value);
    		}
    	}
    	
    	@Override
    	public String getHumanReadableEquation() throws MathUtilException{
    		assert(Operator.getSymbolList().contains(value));
    		assert(left != null);
    		assert(right != null);
    		String result = getHumanReadable();
    		return result.substring(1, result.length()-1); //Strip outtermost parentheses
    	}
    	
    	@Override
    	public long getCorrectAnswer() throws MathUtilException{
    		assert(Operator.getSymbolList().contains(value));
    		assert(left != null);
    		assert(right != null);
    		return evaluate();
    	}
    }
	
	public static class MathUtilException extends Exception{
		private static final long serialVersionUID = -1969599743336198987L;
		public MathUtilException(String msg) {
			super(msg);
		}
	}
}
