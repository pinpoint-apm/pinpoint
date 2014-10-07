package com.nhn.pinpoint.profiler.interceptor.bci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class TestObject {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private int callA;
	private boolean isthrow = false;
	private int returnCode = 1;

	public void setIsthrow(boolean isthrow) {
		this.isthrow = isthrow;
	}

	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	public int callA() {
        logger.info("callA");
        int i = callA++;
		if (isthrow) {
			throw new RuntimeException("ddd");
		}
		System.out.println("callA");
		if(returnCode == 1) {
			return 1;
		} if (returnCode == 10){
			return  -1;
		}
		return 0;
    }

	public static void before() {
		System.out.println("before");
	}
	public static void after() {
		System.out.println("after");
	}
	public static void callCatch() {
		System.out.println("callCatch");
	}

    public String hello(String a) {
        System.out.println("a:" + a);
        System.out.println("test");
//        throw new RuntimeException("test");
        return "a";
    }

}
