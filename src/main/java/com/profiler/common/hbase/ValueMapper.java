package com.profiler.common.hbase;

/**
 *
 */
public interface ValueMapper<T> {
    byte[] mapValue(T value);
}
