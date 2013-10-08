package com.nhn.pinpoint.collector.util;

/**
 *
 */
public interface ObjectPoolFactory<T> {
    T create();

    void beforeReturn(T t);
}
