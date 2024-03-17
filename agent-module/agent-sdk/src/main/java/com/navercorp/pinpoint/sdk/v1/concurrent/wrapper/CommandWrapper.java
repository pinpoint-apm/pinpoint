package com.navercorp.pinpoint.sdk.v1.concurrent.wrapper;

import java.util.Collection;
import java.util.concurrent.Callable;

public interface CommandWrapper {
    Runnable wrap(Runnable command);

    <T> Callable<T> wrap(Callable<T> callable);

    <T> Collection<? extends Callable<T>> wrap(Collection<? extends Callable<T>> tasks);
}
