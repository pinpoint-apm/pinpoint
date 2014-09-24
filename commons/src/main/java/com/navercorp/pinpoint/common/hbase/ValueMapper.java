package com.nhn.pinpoint.common.hbase;

/**
 * @author emeroad
 */
public interface ValueMapper<T> {
    byte[] mapValue(T value);
}
