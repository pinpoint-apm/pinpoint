package com.navercorp.pinpoint.common.timeseries.array;

import com.google.common.primitives.Ints;

import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToIntFunction;

public final class IntArray {

    private IntArray() {
    }

    public static <T> int[] asIntArray(List<T> list, ToIntFunction<T> function) {
        if (list instanceof RandomAccess) {
            final int size = list.size();
            final int[] values = new int[size];
            for (int i = 0; i < size; i++) {
                values[i] = function.applyAsInt(list.get(i));
            }
            return values;
        }

        return asIntArray((Collection<T>) list, function);
    }

    public static <T> int[] asIntArray(Collection<T> list, ToIntFunction<T> function) {
        final int size = list.size();
        final int[] values = new int[size];
        int i = 0;
        for (T element : list) {
            values[i++] = function.applyAsInt(element);
        }
        return values;
    }

    public static <T> List<Integer> asList(List<T> list, ToIntFunction<T> function) {
        return Ints.asList(asIntArray(list, function));
    }
}
