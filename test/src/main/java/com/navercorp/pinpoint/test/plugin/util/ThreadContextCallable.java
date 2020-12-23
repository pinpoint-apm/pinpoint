package com.navercorp.pinpoint.test.plugin.util;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.concurrent.Callable;

public class ThreadContextCallable<V> implements Callable<V> {

    private final Callable<V> delegate;
    private final ClassLoader contextClassLoader;

    public ThreadContextCallable(Callable<V> delegate, ClassLoader contextClassLoader) {
        this.delegate = Assert.requireNonNull(delegate, "delegate");
        this.contextClassLoader = Assert.requireNonNull(contextClassLoader, "contextClassLoader");
    }

    public V call() throws Exception {
        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(contextClassLoader);
        try {
            return delegate.call();
        } finally {
            thread.setContextClassLoader(before);
        }
    }

}
