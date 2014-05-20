package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public interface SerializerFactory {
    HeaderTBaseSerializer createSerializer();
}
