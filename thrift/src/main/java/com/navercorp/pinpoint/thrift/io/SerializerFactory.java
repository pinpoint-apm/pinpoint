package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public interface SerializerFactory<E> {
    E createSerializer();
}
