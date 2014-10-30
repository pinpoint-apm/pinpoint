package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
public class ThreadLocalHeaderTBaseDeserializerFactory<E> implements DeserializerFactory<E> {

    private final ThreadLocal<E> cache = new ThreadLocal<E>() {
        @Override
        protected E initialValue() {
            return factory.createDeserializer();
        }
    };

    private final DeserializerFactory<E> factory;

    public ThreadLocalHeaderTBaseDeserializerFactory(DeserializerFactory<E> factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        this.factory = factory;
    }

    @Override
    public E createDeserializer() {
        return cache.get();
    }
}
