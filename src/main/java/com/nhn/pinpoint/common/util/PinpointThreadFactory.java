package com.nhn.pinpoint.common.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class PinpointThreadFactory implements ThreadFactory {

    private final static AtomicInteger FACTORY_NUMBER = new AtomicInteger(0);
    private final AtomicInteger threadNumber = new AtomicInteger(0);

    private String factoryName;
    private String threadName;
    private boolean daemon;


    public PinpointThreadFactory() {
        this("Pinpoint", false);
    }

    public PinpointThreadFactory(String threadName) {
        this(threadName, false);
    }

    public PinpointThreadFactory(String threadName, boolean daemon) {
        if (threadName == null) {
            throw new NullPointerException("threadName");
        }
        this.threadName = threadName;
        this.factoryName = Integer.toString(FACTORY_NUMBER.getAndIncrement());
        this.daemon = daemon;
    }

    public void setThreadName(String threadName) {
        if (threadName == null) {
            throw new NullPointerException("threadName");
        }
        this.threadName = threadName;
    }

    @Override
    public Thread newThread(Runnable job) {
        String newThreadName = createThreadName();
        Thread thread = new Thread(job, newThreadName);
        if (daemon) {
            thread.setDaemon(daemon);
        }
        return thread;
    }

    private String createThreadName() {
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(threadName);
        buffer.append('(');
        buffer.append(factoryName);
        buffer.append('-');
        buffer.append(threadNumber.getAndIncrement());
        buffer.append(')');
        return buffer.toString();
    }

    public static ThreadFactory createThreadFactory(String threadName) {
        return createThreadFactory(threadName, false);
    }

    public static ThreadFactory createThreadFactory(String threadName, boolean daemon) {
        return new PinpointThreadFactory(threadName, daemon);
    }
}
