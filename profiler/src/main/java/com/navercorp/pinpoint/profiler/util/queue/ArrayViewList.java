/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.util.queue;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.RandomAccess;

public class ArrayViewList<E> extends AbstractList<E> implements RandomAccess, java.io.Serializable
{
    private final E[] array;
    private final int endIndex;

    public ArrayViewList(E[] array, int endIndex) {
        if (endIndex > array.length) {
            throw new ArrayIndexOutOfBoundsException("End index must not be greater than the array length");
        }
        this.array = Objects.requireNonNull(array);
        this.endIndex = endIndex;
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return endIndex;
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(array, endIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = size();
        if (a.length < size) {
            return Arrays.copyOf(this.array, size, (Class<? extends T[]>) a.getClass());
        }
        System.arraycopy(this.array, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public E get(int index) {
        if (index > endIndex) {
            throw new ArrayIndexOutOfBoundsException("End index must not be greater than the array length");
        }
        return this.array[index];
    }

    @Override
    public E set(int index, E element) {
        if (index > endIndex) {
            throw new ArrayIndexOutOfBoundsException("End index must not be greater than the array length");
        }
        E old = this.array[index];
        this.array[index] = element;
        return old;
    }

    @Override
    public int indexOf(Object element) {
        E[] copy = this.array;
        if (element == null) {
            for (int i = 0; i < endIndex; i++) {
                if (copy[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < endIndex; i++) {
                if (element.equals(copy[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public Iterator<E> iterator() {
        return new ObjectArrayIterator<>(array, endIndex);
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }


    @Override
    public void sort(Comparator<? super E> c) {
        Arrays.sort(array, 0, endIndex, c);
    }
}