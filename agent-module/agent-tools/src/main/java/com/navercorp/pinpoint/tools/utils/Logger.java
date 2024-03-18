package com.navercorp.pinpoint.tools.utils;

public final class Logger {
    public void info(CharSequence log) {
        System.out.println(log);
    }

    public void info(Throwable th) {
        th.printStackTrace();
    }
}
