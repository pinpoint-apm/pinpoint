package com.nhn.pinpoint.bootstrap.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public final class InterceptorUtils {
    private InterceptorUtils() {
    }

    public static boolean isThrowable(Object result) {
        return result instanceof Throwable;
    }

    public static boolean isSuccess(Throwable throwable) {
        return throwable == null;
    }


    public static String exceptionToString(Throwable ex) {
		if (ex != null) {
			StringBuilder sb = new StringBuilder(128);
			sb.append(ex.toString()).append("\n");

			Writer writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
			sb.append(writer.toString());

			return sb.toString();
		}
		return null;
	}
}
