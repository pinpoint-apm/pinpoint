package com.profiler.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class InterceptorUtils {
	public static boolean isThrowable(Object result) {
        return result instanceof Throwable;
    }

	public static boolean isSuccess(Object result) {
		return !isThrowable(result);
	}

	public static String exceptionToString(Exception e) {
		if (e != null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(e.toString()).append("\n");

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			sb.append(writer.toString());

			return sb.toString();
		}
		return null;
	}
}
