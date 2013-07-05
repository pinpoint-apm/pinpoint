package com.nhn.pinpoint.collector.util;

import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
public class FixedPool<T> {

    private final LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    private final FixedPoolFactory<T> factory;

    public FixedPool(FixedPoolFactory<T> factory, int size) {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factory = factory;
        fill(size);
    }

    private void fill(int size) {
        for (int i = 0; i < size; i++) {
            T t = this.factory.create();
            queue.offer(t);
        }
    }

    public T getObject() {
        return queue.poll();
    }

    public void returnObject(T t) {
        factory.beforeReturn(t);
        queue.offer(t);
    }


}
