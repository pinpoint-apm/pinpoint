package com.navercorp.pinpoint.common.hbase.async;

import java.util.function.BiConsumer;

public interface LimiterHelper {
    boolean acquire(int permits);

    void release(int permits);

    <R> BiConsumer<R, Throwable> release();
}
