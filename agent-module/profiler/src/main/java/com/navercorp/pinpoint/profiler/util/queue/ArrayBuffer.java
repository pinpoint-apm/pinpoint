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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.RandomAccess;

public class ArrayBuffer<E> {
    private final int bufferSize;

    private E[] buffer;
    private int offset;

    public ArrayBuffer(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public void put(List<E> elements) {
        if (this.buffer == null) {
            this.buffer = newBuffer(Math.max(elements.size(), bufferSize));
        }

        checkExpand(elements.size());

        if (elements instanceof RandomAccess) {
            for (int i = 0; i < elements.size(); i++) {
                buffer[offset++] = elements.get(i);
            }
        } else {
            for (E element : elements) {
                buffer[offset++] = element;
            }
        }
    }

    private void checkExpand(int capacity) {
        final int remain = remaining();
        if (remain >= capacity) {
            return;
        }
        final int length = Math.max(buffer.length, 1);
        final int expandedBufferSize = computeExpandedBufferSize(capacity, length, remain);
        this.buffer = Arrays.copyOf(this.buffer, expandedBufferSize);
    }

    private int computeExpandedBufferSize(final int size, int length, int remain) {
        int expandedBufferSize = 0;
        while (remain < size) {
            length <<= 1;
            expandedBufferSize = length;
            remain = expandedBufferSize - offset;
        }
        return expandedBufferSize;
    }

    private int remaining() {
        return this.buffer.length - offset;
    }

    public void put(E e) {
        if (this.buffer == null) {
            this.buffer = newBuffer(bufferSize);
        }
        checkExpand(1);
        buffer[offset++] = e;
    }

    public boolean isOverflow() {
        return size() >= bufferSize;
    }

    @SuppressWarnings("Unchecked")
    private E[] newBuffer(int size) {
        this.offset = 0;
        return (E[]) new Object[size];
    }



    public List<E> drain() {
        if (offset == 0) {
            return Collections.emptyList();
        }
        final E[] oldBuffer = this.buffer;
        this.buffer = null;

        final int currentIndex = this.offset;
        this.offset = 0;

        return new ArrayViewList<>(oldBuffer, currentIndex);
    }


    public int size() {
        return offset;
    }

    @Override
    public String toString() {
        return "ArrayBuffer{" +
                "bufferSize=" + bufferSize +
                ", offset=" + offset +
                ", buffer=" + Arrays.toString(buffer) +
                '}';
    }
}
