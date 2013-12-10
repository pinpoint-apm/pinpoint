package com.nhn.pinpoint.web.filter;

/**
 * FIXME 데모하려고 급조한 클래스.
 * 
 * @author netspider
 * 
 */
public enum CompareOperators {
	EQ, GT, LT;

	public boolean compare(long a, long b) {
		if (this == EQ) {
			return a == b;
		} else if (this == GT) {
			return a > b;
		} else {
			return a < b;
		}
	}
}
