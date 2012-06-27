package com.profiler.trace;

public abstract class AbstractTracer {
	public static void printStackTrace() {
		log("#################################################");
		StackTraceElement[] stackList=Thread.currentThread().getStackTrace();
		int length=stackList.length;
		for(int loop=2;loop<length;loop++) {
			log(stackList[loop].toString());
		}
	}
	protected static void log(String message) {
		System.out.println("*** "+message);
	}
}
