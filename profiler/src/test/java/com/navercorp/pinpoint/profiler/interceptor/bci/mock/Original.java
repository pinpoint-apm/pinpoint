package com.nhn.pinpoint.profiler.interceptor.bci.mock;

import java.util.Collections;
import java.util.Map;

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
	}

	public int testSignatureMiss() {
		return -1;
	}

	public void testMethodCall() {
	}

	public Map<String, String> testGeneric() {
		return Collections.emptyMap();
	}

}
