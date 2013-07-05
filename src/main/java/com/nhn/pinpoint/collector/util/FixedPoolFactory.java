package com.nhn.pinpoint.collector.util;

/**
 *
 */
public interface FixedPoolFactory<T> {
    T create();

    void beforeReturn(T t);
}
