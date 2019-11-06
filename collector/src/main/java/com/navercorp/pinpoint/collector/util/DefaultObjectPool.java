/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.util;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author emeroad
 */
public class DefaultObjectPool<T> implements ObjectPool<T> {

    // you don't need a blocking queue. There must be enough objects in a queue. if not, it means leakage.
    private final Queue<PooledObject<T>> queue = new ConcurrentLinkedQueue<>();

    private final ObjectPoolFactory<T> factory;

    public DefaultObjectPool(ObjectPoolFactory<T> factory, int size) {
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factory = factory;
        fill(size);
    }

    private void fill(int size) {
        for (int i = 0; i < size; i++) {
            PooledObjectWrapper wrapper = createObject();
            queue.offer(wrapper);
        }
    }

    private PooledObjectWrapper createObject() {
        T t = this.factory.create();
        return new PooledObjectWrapper(t);
    }

    @Override
    public PooledObject<T> getObject() {
        PooledObject<T> object = queue.poll();
        if (object == null) {
            // create dynamically ???
            return createObject();
        }
        return object;
    }


    private void returnObject(PooledObject<T> t) {
        if (t == null) {
            return;
        }
        factory.beforeReturn(t.getObject());
        queue.offer(t);
    }

    public int size() {
        return queue.size();
    }

    private class PooledObjectWrapper implements PooledObject<T> {
        private final T value;

        public PooledObjectWrapper(T value) {
            this.value = Objects.requireNonNull(value, "value");
        }

        @Override
        public T getObject() {
            return value;
        }

        @Override
        public void returnObject() {
            DefaultObjectPool.this.returnObject(this);
        }
    }


}
