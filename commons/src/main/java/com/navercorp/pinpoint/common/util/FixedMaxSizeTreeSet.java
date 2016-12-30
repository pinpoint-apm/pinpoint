/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * @author Taejin Koo
 */
public class FixedMaxSizeTreeSet<E> {

    private final int capacity;
    private final Comparator<? super E> comparator;

    private final TreeSet<E> treeSet;

    public FixedMaxSizeTreeSet(int capacity, Comparator<? super E> comparator) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positivenumber");
        }

        if (comparator == null) {
            throw new NullPointerException("comparator may not be null");
        }

        this.capacity = capacity;
        this.comparator = comparator;

        this.treeSet = new TreeSet<E>(comparator);
    }

    public boolean add(E element) {
        if (element == null) {
            throw new NullPointerException("element may not be null");
        }

        if (treeSet.size() < capacity) {
            return treeSet.add(element);
        }

        if (comparator.compare(treeSet.last(), element) > 0) {
            treeSet.pollLast();
            return treeSet.add(element);
        }

        return false;
    }

    public List<E> getList() {
        return new ArrayList<E>(treeSet);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("FixedMaxSizeTreeSet{");
        sb.append("capacity=").append(capacity);
        sb.append(", innerSet=").append(treeSet);
        sb.append('}');
        return sb.toString();
    }

}
