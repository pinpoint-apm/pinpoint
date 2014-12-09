package com.navercorp.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public interface DeserializerFactory<E> {
    E createDeserializer();
}
