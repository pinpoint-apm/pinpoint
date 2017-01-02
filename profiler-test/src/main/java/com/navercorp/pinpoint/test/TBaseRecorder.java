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

package com.navercorp.pinpoint.test;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.thrift.TBase;

/**
 * @author hyungil.jeong
 */
public class TBaseRecorder<T extends TBase<?, ?>> implements Iterable<T> {

    private final Queue<T> queue = new ConcurrentLinkedQueue<T>();

    public T peek() {
        return this.queue.peek();
    }

    public T poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }

    public void clear() {
        this.queue.clear();
    }

    @Override
    public Iterator<T> iterator() {
        return this.queue.iterator();
    }

    public boolean add(T data) {
        return this.queue.offer(data);
    }


    @Override
    public String toString() {
        return this.queue.toString();
    }

}
