package com.navercorp.pinpoint.common.server.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Formatter;

public final class ServerBootLogger {

    private final String loggerName;
    private final PrintStream out = System.out;
    private final PrintStream err = System.err;
    
    private static final String FORMAT = "%tm-%<td %<tT.%<tL %-5s %-35.35s : %s";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private ServerBootLogger(String loggerName) {
        this.loggerName = loggerName;
    }

    public static ServerBootLogger getLogger(String name) {
        return new ServerBootLogger(name);
    }

    public static ServerBootLogger getLogger(Class<?> clazz) {
        return new ServerBootLogger(clazz.getSimpleName());
    }

    public void info(String msg) {
        String formatMessage = format("INFO", msg, null);
        this.out.print(formatMessage);
    }


    protected String format(String logLevel, String msg, Throwable throwable) {
        final long now = System.currentTimeMillis();

        StringBuilder buffer = new StringBuilder(64);
        Formatter formatter = new Formatter(buffer);
        formatter.format(FORMAT, now, logLevel, loggerName, msg);
        if (throwable != null) {
            String exceptionMessage = toString(throwable);
            buffer.append(exceptionMessage);
        }  else {
            buffer.append(LINE_SEPARATOR);
        }


        return formatter.toString();
    }

    public void error(String msg) {
        String formatMessage = format("ERROR", msg, null);
        this.out.print(formatMessage);
    }

    public void error(String msg, Throwable th) {
        String formatMessage = format("ERROR", msg, th);
        this.err.print(formatMessage);
    }

    private static String toString(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter(512);
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        throwable.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

}
