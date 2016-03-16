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

package com.navercorp.pinpoint.bootstrap.interceptor.registry;

import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * @author emeroad
 */
public final class WeakAtomicReferenceArray<T> {

    private final int length;
    private final AtomicReferenceArray<T> atomicArray;
//    private final T[] array;

    public WeakAtomicReferenceArray(int length, Class<? extends T> clazz) {
        this.length = length;
        this.atomicArray = new AtomicReferenceArray<T>(length);
//        this.array = (T[]) Array.newInstance(clazz, length);
    }

    public void set(int index, T newValue) {
        this.atomicArray.set(index, newValue);
        // need TestCase ~~
//        this.array[index] = newValue;
    }

    public int length() {
        return length;
    }


    public T get(int index) {
        // try not thread safe read  -> fail -> thread safe read
//        final T unsafeValue = this.array[index];
//        if (unsafeValue != null) {
//            return unsafeValue;
//        }
//        return (T) this.array[index];

        // thread safe read
        return this.atomicArray.get(index);
    }

}
