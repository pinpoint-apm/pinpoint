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

package com.navercorp.pinpoint.profiler.sender;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * @author emeroad
 */
class UnsafeArrayCollection<E> extends AbstractCollection<E> {

    private int size = 0;
    private final Object[] array;

    public UnsafeArrayCollection(int maxSize) {
        this.array = new Object[maxSize];
    }


    @Override
    public boolean add(E o) {
        if (array.length < size) {
            throw new IndexOutOfBoundsException("size:" + this.size + " array.length:" + array.length);
        }
        // do not check array bound
        array[size] = o;
        size++;
        return true;
    }

    @Override
    public void clear() {
        // Need to clear values in array. It costs CPU but prevent memory leak.
        for (int i = 0; i < size; i++) {
            this.array[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Object[] toArray() {
        // return internal array
        return array;
    }
}
