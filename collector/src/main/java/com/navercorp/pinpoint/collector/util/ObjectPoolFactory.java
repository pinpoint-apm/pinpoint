package com.navercorp.pinpoint.collector.util;

/**
 * @author emeroad
 */
public interface ObjectPoolFactory<T> {
    T create();

    void beforeReturn(T t);
}
