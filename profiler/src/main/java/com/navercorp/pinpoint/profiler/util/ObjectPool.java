package com.navercorp.pinpoint.profiler.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author emeroad
 */
public class ObjectPool<T> {

    // you don't need a blocking queue. There must be enough objects in a queue.
    // if not, it means leakage.
    private final Queue<T> queue = new ConcurrentLinkedQueue<T>();

    private final ObjectPoolFactory<T> factory;

    public ObjectPool(ObjectPoolFactory<T> factory, int size) {
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
        T object = queue.poll();
        if (object == null) {
            // create dynamically
            return factory.create();
        }
        return object;
    }

    public void returnObject(T t) {
        if (t == null) {
            return;
        }
        factory.beforeReturn(t);
        queue.offer(t);
    }

}
