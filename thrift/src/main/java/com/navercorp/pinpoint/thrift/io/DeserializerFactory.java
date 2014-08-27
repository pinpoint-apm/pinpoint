package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public interface DeserializerFactory {
    HeaderTBaseDeserializer createDeserializer();
}
