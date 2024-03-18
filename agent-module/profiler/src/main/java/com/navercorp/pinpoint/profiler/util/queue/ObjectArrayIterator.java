/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.profiler.util.queue;

import java.util.Iterator;
import java.util.NoSuchElementException;

// copy from apache collection4
/**
 * An {@link java.util.Iterator Iterator} over an array of objects.
 * <p>
 * This iterator does not support {@link #remove}, as the object array cannot be
 * structurally modified.
 * <p>
 *
 * @param <E> the type of elements returned by this iterator
 * @since 3.0
 */
public class ObjectArrayIterator<E> implements Iterator<E> {

    /** The array */
    private final E[] array;
    /** The start index to loop from */
    /** The end index to loop to */
    private final int endIndex;
    /** The current iterator index */
    private int index = 0;


    /**
     * Constructs an ObjectArrayIterator that will iterate over the values in the
     * specified array from a specific start index.
     *
     * @param array  the array to iterate over
     * @throws NullPointerException if <code>array</code> is <code>null</code>
     * @throws IndexOutOfBoundsException if the start index is out of bounds
     */
    public ObjectArrayIterator(final E array[]) {
        this(array, array.length);
    }

    /**
     * Construct an ObjectArrayIterator that will iterate over a range of values
     * in the specified array.
     *
     * @param array  the array to iterate over
     * @param end  the index (exclusive) to finish iterating at
     * @throws IndexOutOfBoundsException if the start or end index is out of bounds
     * @throws IllegalArgumentException if end index is before the start
     * @throws NullPointerException if <code>array</code> is <code>null</code>
     */
    public ObjectArrayIterator(final E array[], final int end) {
        super();
        if (end > array.length) {
            throw new ArrayIndexOutOfBoundsException("End index must not be greater than the array length");
        }

        this.array = array;
        this.endIndex = end;
        this.index = 0;
    }

    // Iterator interface
    //-------------------------------------------------------------------------

    /**
     * Returns true if there are more elements to return from the array.
     *
     * @return true if there is a next element to return
     */
    @Override
    public boolean hasNext() {
        return this.index < this.endIndex;
    }

    /**
     * Returns the next element in the array.
     *
     * @return the next element in the array
     * @throws NoSuchElementException if all the elements in the array
     *    have already been returned
     */
    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return this.array[this.index++];
    }

}
