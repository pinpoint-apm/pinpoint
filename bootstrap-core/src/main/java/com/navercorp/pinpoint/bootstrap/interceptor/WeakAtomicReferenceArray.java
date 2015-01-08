package com.navercorp.pinpoint.bootstrap.interceptor;

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

    public final void set(int index, T newValue) {
        this.atomicArray.set(index, newValue);
        // need TestCase ~~
//        this.array[index] = newValue;
    }

    public final int length() {
        return length;
    }


    public final T get(int index) {
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
