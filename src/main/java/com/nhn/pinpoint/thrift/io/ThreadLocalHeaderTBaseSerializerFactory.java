package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public class ThreadLocalHeaderTBaseSerializerFactory implements SerializerFactory {

    private final ThreadLocal<HeaderTBaseSerializer> cache = new ThreadLocal<HeaderTBaseSerializer>() {
        @Override
        protected HeaderTBaseSerializer initialValue() {
            return factory.createSerializer();
        }
    };

    private final SerializerFactory factory;

    public ThreadLocalHeaderTBaseSerializerFactory(SerializerFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        this.factory = factory;
    }


    @Override
    public HeaderTBaseSerializer createSerializer() {
        return cache.get();
    }
}
