package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public class ThreadLocalHeaderTBaseDeserializerFactory implements DeserializerFactory {

    private final ThreadLocal<HeaderTBaseDeserializer> cache = new ThreadLocal<HeaderTBaseDeserializer>() {
        @Override
        protected HeaderTBaseDeserializer initialValue() {
            return factory.createDeserializer();
        }
    };

    private final DeserializerFactory factory;

    public ThreadLocalHeaderTBaseDeserializerFactory(DeserializerFactory factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        this.factory = factory;
    }

    @Override
    public HeaderTBaseDeserializer createDeserializer() {
        return cache.get();
    }
}
