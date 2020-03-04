/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.util.Assert;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * @author Woonduk Kang(emeroad)
 */
class LimitedList<E> implements Collection<E> {
    private final int maxSize;
    private Comparator<E> comparator;
    private final Queue<E> queue;

    public LimitedList(int maxSize, Comparator<E> comparator) {
        Assert.isTrue(maxSize > 0, "maxSize must not be negative");
        this.comparator = Assert.requireNonNull(comparator, "comparator");

        this.maxSize = maxSize;
        this.queue = new PriorityQueue<E>(maxSize, comparator);

    }


    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.queue.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.queue.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.queue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.queue.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return this.queue.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.queue.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.queue.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.queue.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.queue.retainAll(c);
    }

    @Override
    public void clear() {
        this.queue.clear();
    }

    @Override
    public boolean add(E e) {
        if (queue.size() >= maxSize) {
            final E minSnapshot = queue.peek();
            if (comparator.compare(minSnapshot, e) < 0) {
                queue.poll();
                return queue.offer(e);
            }

            return false;
        } else {
            return queue.add(e);
        }
    }

}
