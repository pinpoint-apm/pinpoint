/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.java9.classloader;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * @author Woonduk Kang(emeroad)
 */
class MergedEnumeration2<E> implements Enumeration<E> {
    private static final int MAX = 2;

    private final Enumeration<E> enum0;
    private final Enumeration<E> enum1;
    private int index = 0;

    public MergedEnumeration2(Enumeration<E> enum0, Enumeration<E> enum1) {
        this.enum0 = enum0;
        this.enum1 = enum1;
    }

    private boolean next() {
        while (this.index < MAX) {
            final Enumeration<E> enumeration = getEnumeration();
            if (enumeration != null && enumeration.hasMoreElements()) {
                return true;
            }

            nextIndex();
        }

        return false;
    }

    private void nextIndex() {
        this.index++;
    }

    private Enumeration<E> getEnumeration() {
        switch (index) {
            case 0:
                return enum0;
            case 1:
                return enum1;
            default:
                throw new NoSuchElementException("index out of range:" + index);
        }
    }

    public boolean hasMoreElements() {
        return this.next();
    }

    public E nextElement() {
        if (!this.next()) {
            throw new NoSuchElementException();
        }

        Enumeration<E> enumeration = getEnumeration();
        return enumeration.nextElement();
    }

}
