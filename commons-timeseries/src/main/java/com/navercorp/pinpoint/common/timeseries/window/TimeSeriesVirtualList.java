package com.navercorp.pinpoint.common.timeseries.window;

import com.google.common.base.Preconditions;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Collection;
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
        Preconditions.checkArgument(size >= 0, "negative size");
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

    @Override
    public Object[] toArray() {
        final Long[] longs = new Long[size];
        copyOfTimeSeries(longs);
        return longs;
    }


    @SuppressWarnings("unchecked")
    private <T> void copyOfTimeSeries(T[] longs) {
        long value = start;
        for (int i = 0; i < size; i++) {
            longs[i] = (T) Long.valueOf(value);
            value += increment;
        }
    }


    @Override
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            @SuppressWarnings("unchecked")
            final T[] newArray = ((T[]) Array.newInstance(a.getClass().getComponentType(), size));
            copyOfTimeSeries(newArray);
            return newArray;
        }
        copyOfTimeSeries(a);
        return a;
    }

    // for debug -----------

    @Override
    public Long set(int index, Long element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Long element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, Long element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Long remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends Long> c) {
        throw new UnsupportedOperationException();
    }


}