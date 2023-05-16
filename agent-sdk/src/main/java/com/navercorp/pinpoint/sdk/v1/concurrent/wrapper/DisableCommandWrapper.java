package com.navercorp.pinpoint.sdk.v1.concurrent.wrapper;

import java.util.Collection;
import java.util.concurrent.Callable;

public class DisableCommandWrapper implements CommandWrapper {
    @Override
    public Runnable wrap(Runnable command) {
        return command;
    }

    @Override
    public <T> Callable<T> wrap(Callable<T> callable) {
        return callable;
    }

    @Override
    public <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks) {
        return tasks;
    }
}
