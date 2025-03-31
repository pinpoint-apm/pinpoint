package com.navercorp.pinpoint.common.server.util.timewindow;

import org.springframework.util.Assert;

import java.util.AbstractList;
import java.util.Iterator;

/**
 * A virtual list that represents a time series.
 * @author emeroad
 */
public class TimeSeriesVirtualList extends AbstractList<Long> {
    private final int size;
    private final long start;
    private final long increment;

    public TimeSeriesVirtualList(int size, long start, long increment) {
        Assert.isTrue(size >= 0, "negative size");
        this.size = size;
        this.start = start;
        this.increment = increment;
    }

    @Override
    public Long get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        return start + (index * increment);
    }

    @Override
    public Iterator<Long> iterator() {
        return new TimeSeriesIterator(this.size, this.start, this.increment);
    }

    @Override
    public int size() {
        return size;
    }
}