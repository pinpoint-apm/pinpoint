package com.navercorp.pinpoint.common.server.util;

import java.io.PrintStream;

public final class ServerBootLogger {

    private final String name;
    private final PrintStream out = System.out;

    private ServerBootLogger(String name) {
        this.name = name;
    }

    public static ServerBootLogger getLogger(String name) {
        return new ServerBootLogger(name);
    }

    public static ServerBootLogger getLogger(Class<?> clazz) {
        return new ServerBootLogger(clazz.getSimpleName());
    }

    public void info(String msg) {
        log("INFO", msg);
    }

    private void log(String level, String msg) {
        msg = format(level, msg);
        this.out.println(msg);
    }

    protected String format(String level, String msg) {
        long now = System.currentTimeMillis();
        return String.format("%tm-%<td %<tT.%<tL %s %35.35s : %s", now, level, name, msg);
    }

    public void error(String msg) {
        log("ERROR", msg);
    }

    public void error(String msg, Throwable th) {
        log("ERROR", msg);
        th.printStackTrace(out);
    }

}
