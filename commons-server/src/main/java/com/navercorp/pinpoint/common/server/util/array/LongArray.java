package com.navercorp.pinpoint.common.server.util.array;

import com.google.common.primitives.Longs;

import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToLongFunction;

public final class LongArray {

    private LongArray() {
    }

    public static <T> List<Long> asList(List<T> list, ToLongFunction<T> function) {
        final int size = list.size();
        final long[] values = new long[size];
        if (list instanceof RandomAccess) {
            for (int i = 0; i < size; i++) {
                values[i] = function.applyAsLong(list.get(i));
            }
        } else {
            int i = 0;
            for (T element : list) {
                values[i++] = function.applyAsLong(element);
            }
        }
        return Longs.asList(values);
    }

}
