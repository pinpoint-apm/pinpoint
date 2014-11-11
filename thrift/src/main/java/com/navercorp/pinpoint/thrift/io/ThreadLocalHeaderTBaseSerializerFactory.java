package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 * @author jaehong.kim
 *   - change to generic type
 */
public class ThreadLocalHeaderTBaseSerializerFactory<E> implements SerializerFactory<E> {

    private final ThreadLocal<E> cache = new ThreadLocal<E>() {
        @Override
        protected E initialValue() {
            return factory.createSerializer();
        }
    };

    private final SerializerFactory<E> factory;

    public ThreadLocalHeaderTBaseSerializerFactory(SerializerFactory<E> factory) {
        if (factory == null) {
            throw new NullPointerException("factory must not be null");
        }
        this.factory = factory;
    }

    @Override
    public E createSerializer() {
        return cache.get();
    }
}
