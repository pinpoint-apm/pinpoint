package com.navercorp.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public interface SerializerFactory<E> {
    E createSerializer();
}
