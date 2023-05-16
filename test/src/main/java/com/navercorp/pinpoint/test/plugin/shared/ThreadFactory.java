package com.navercorp.pinpoint.test.plugin.shared;

import java.util.Objects;

public class ThreadFactory implements java.util.concurrent.ThreadFactory {
    private final String threadName;
    private final ClassLoader cl;

    public ThreadFactory(String threadName, ClassLoader cl) {
        this.threadName = Objects.requireNonNull(threadName, "threadName");
        this.cl = Objects.requireNonNull(cl, "cl");
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName(threadName);
        thread.setContextClassLoader(cl);
        thread.setDaemon(true);
        return thread;
    }
}
