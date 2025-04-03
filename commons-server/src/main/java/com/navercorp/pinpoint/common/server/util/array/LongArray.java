package com.navercorp.pinpoint.common.server.util.array;

import com.google.common.primitives.Longs;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToLongFunction;

public final class LongArray {

    private LongArray() {
    }

    public static <T> long[] asLongArray(List<T> list, ToLongFunction<T> function) {
        if (list instanceof RandomAccess) {
            final int size = list.size();
            final long[] values = new long[size];
            for (int i = 0; i < size; i++) {
                values[i] = function.applyAsLong(list.get(i));
            }
            return values;
        }
        return asLongArray((Collection<T>) list, function);
    }

    public static <T> long[] asLongArray(Collection<T> list, ToLongFunction<T> function) {
        final int size = list.size();
        final long[] values = new long[size];
        int i = 0;
        for (T element : list) {
            values[i++] = function.applyAsLong(element);
        }
        return values;
    }

    public static <T> List<Long> asList(List<T> list, ToLongFunction<T> function) {
        return Longs.asList(asLongArray(list, function));
    }
}
