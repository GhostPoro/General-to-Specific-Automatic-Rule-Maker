package core;

import java.util.ArrayList;
import java.util.List;

public class Conclusion {
	
	private int result;
	private double score;
	private boolean valid = false;
	private int[] params;
	private List<Integer> coveredRows;
	
	public Conclusion(int result, boolean valid) {
		this(result, 0, valid);
	}
	
	public Conclusion(int result, double score, boolean valid) {
		this(result, score, valid, new int[0]);
	}
	
	public Conclusion(int result, double score, boolean valid, int[] params) {
		this.result = result;
		this.valid = valid;
		this.params = params;
		this.score= score;
	}
	
	public Conclusion addParam(int colNum, int colName) {
		if (params == null) {
			params = new int[0];
		}
		
		int oldSize = params.length;
		
		int[] newArray = new int[oldSize + 2];
		
		for (int i = 0; i < oldSize; i++) {
			newArray[i] = params[i];
		}
		
		params = newArray;
		params[oldSize] = colNum;
		params[oldSize + 1] = colName;
		
		return this;
	}
	
	public int[] getParam() {
		return params;
	}

	public int getResult() {
		return result;
	}

	public Conclusion setResult(int result) {
		this.result = result;
		return this;
	}
	
	public double getScore() {
		return score;
	}

	public Conclusion setScore(double inScore) {
		this.score = inScore;
		return this;
	}

	public List<Integer> getCoveredRows() {
		return coveredRows;
	}

	public Conclusion setCoveredRows(List<Integer> coveredRows) {
		this.coveredRows = coveredRows;
		return this;
	}
	
	public Conclusion addCoveredRow(int r) {
		if(this.coveredRows == null) {
			this.coveredRows = new ArrayList<Integer>();
		}
		this.coveredRows.add(r);
		return this;
	}
	
	public String printCoveredRows() {
		String result = "{";
		boolean firsttime = true;
		for(int cr : this.coveredRows) {
			if(firsttime) {
				result += (cr + 1);
				firsttime = false;
			}
			else {
				result += (", " + (cr + 1));
			}
		}
		result += "}";
		return result;
	}

	public boolean isValid() {
		return valid;
	}

	public Conclusion setValid(boolean valid) {
		this.valid = valid;
		return this;
	}
	
	public Conclusion setValid() {
		this.valid = true;
		return this;
	}
	
	
}
