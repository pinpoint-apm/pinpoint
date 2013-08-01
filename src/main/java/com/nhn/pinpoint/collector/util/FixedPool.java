package com.nhn.pinpoint.collector.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class FixedPool<T> {
    // 구지 blocking 할 필요가 없음. queue안에 충분한 양이 무조껀 있어야 됨. 없으면 뭔가 릭임.
    private final Queue<T> queue = new ConcurrentLinkedQueue<T>();

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
        if (t == null) {
            return;
        }
        factory.beforeReturn(t);
        queue.offer(t);
    }


}
