package com.navercorp.pinpoint.test.plugin.util;

import java.util.Objects;

public class ThreadContextExecutor {

    private final ClassLoader context;

    public ThreadContextExecutor(ClassLoader context) {
        // @Nullable
        this.context = context;
    }

    public ClassLoader getClassLoader() {
        return context;
    }

    public void run(RunExecutable executable) {
        Objects.requireNonNull(executable, "executable");

        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(context);
        try {
            executable.run();
        } finally {
            thread.setContextClassLoader(before);
        }
    }


    public <V> V call(CallExecutable<V> callable) {
        Objects.requireNonNull(callable, "callable");

        final Thread thread = Thread.currentThread();
        final ClassLoader before = thread.getContextClassLoader();
        thread.setContextClassLoader(context);
        try {
            return callable.call();
        } finally {
            thread.setContextClassLoader(before);
        }
    }

}
