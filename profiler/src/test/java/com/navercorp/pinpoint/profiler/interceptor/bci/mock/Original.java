package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import com.nhn.pinpoint.profiler.interceptor.aspect.PointCut;

/**
 * @author emeroad
 */
public class Original {

	public int touchVoid = 0;
	public int touchInt = 0;
	public int touchUtil = 0;

	public int touchBefore;
	public int touchAfter;

	public void testVoid() {
		touchVoid++;
	}

	public int getTouchVoid() {
		return touchVoid;
	}

	public int testInt() {
		return ++touchInt;
	}

	public int getTouchInt() {
		return touchInt;
	}


	public String testString() {
		return "testString";
	}


	public int testUtilMethod() {
		return ++touchUtil;
	}



	void touchBefore() {
		touchBefore++;
	}

	public int getTouchBefore() {
		return touchBefore;
	}

	void touchAfter() {
		touchAfter++;
	}

	public int getTouchAfter() {
		return touchAfter;
	}

	public void testNoTouch() {

	}


	public void methodA() {
		System.out.println("---a");
	}

	public void methodB() {
		System.out.println("---b");
	}


	public void testInternalMethod() {
		touchBefore();
		touchAfter();
		//super
		String s = toString();
		System.out.println(s);
	}

	public int testSignatureMiss() {
		return -1;
	}

	public int testInternalTypeMiss() {
		return -1;
	}

}
