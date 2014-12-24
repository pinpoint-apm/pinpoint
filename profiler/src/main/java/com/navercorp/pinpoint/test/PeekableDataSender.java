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

import com.navercorp.pinpoint.profiler.sender.DataSender;

/**
 * @author hyungil.jeong
 */
public class PeekableDataSender<T extends TBase<?, ?>> implements DataSender, Iterable<T> {

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

    @Override
    public boolean send(TBase<?, ?> data) {
        // deepCopy 안 함. 실제로도 네트워크 전송이 늦게되면 다른 datasender 들도 
        // send 할 객체들의 레퍼런스를 그대로 가지고 있기 때문.
        @SuppressWarnings("unchecked")
        T dataToAdd = (T)data;
        return this.queue.offer(dataToAdd);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isNetworkAvailable() {
        return false;
    }

    @Override
    public String toString() {
        return this.queue.toString();
    }

}
