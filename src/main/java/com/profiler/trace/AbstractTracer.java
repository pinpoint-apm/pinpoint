package com.profiler.trace;

import java.util.logging.Logger;

public abstract class AbstractTracer {

	private static final Logger logger = Logger.getLogger(AbstractTracer.class.getName());

	public static void printStackTrace() {
		StackTraceElement[] stackList = Thread.currentThread().getStackTrace();

		int length = stackList.length;

		for (int loop = 2; loop < length; loop++) {
			logger.info("***" + stackList[loop].toString());
		}
	}
}
