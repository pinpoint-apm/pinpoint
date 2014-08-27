package com.nhn.pinpoint.profiler.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StackTraceUtil {

    private static final Logger logger = Logger.getLogger(StackTraceUtil.class.getName());

    public static void printCurrentStackTrace() {
        printCurrentStackTrace(logger);
    }

    public static void printCurrentStackTrace(Logger logger) {
        if (!logger.isLoggable(Level.INFO)) {
            return;
        }

		StackTraceElement[] stackList = Thread.currentThread().getStackTrace();
		int length = stackList.length;
		for (int loop = 2; loop < length; loop++) {
			logger.info("***" + stackList[loop].toString());
		}
	}

}
