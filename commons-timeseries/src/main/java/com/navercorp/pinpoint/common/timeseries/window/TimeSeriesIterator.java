package com.navercorp.pinpoint.common.timeseries.window;

import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author emeroad
 */
public class TimeSeriesIterator implements Iterator<Long> {
    private final int size;
    private final long start;
    private final long increment;

    private int index = 0;

    public TimeSeriesIterator(int size, long start, long increment) {
        Preconditions.checkArgument(size >= 0, "negative size");
        this.size = size;
        this.start = start;
        this.increment = increment;
    }

    @Override
    public boolean hasNext() {
        return index < size;
    }

    @Override
    public Long next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return get(index++);
    }

    private long get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return start + (index * increment);
    }
}

