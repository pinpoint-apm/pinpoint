package com.navercorp.pinpoint.test.plugin.util;

import java.util.Objects;

public class ThreadContextRunnable implements Runnable {

    private final Runnable delegate;
    private final ClassLoader contextClassLoader;

    public ThreadContextRunnable(Runnable delegate, ClassLoader contextClassLoader) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.contextClassLoader = Objects.requireNonNull(contextClassLoader, "contextClassLoader");
    }

    public void run() {
        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(contextClassLoader);
        try {
            delegate.run();
        } finally {
            thread.setContextClassLoader(before);
        }
    }

}
