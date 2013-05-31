package com.nhn.pinpoint.common.hbase;

/**
 *
 */
public interface ValueMapper<T> {
    byte[] mapValue(T value);
}
