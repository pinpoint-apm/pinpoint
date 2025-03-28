package com.navercorp.pinpoint.common.server.util.array;

import com.google.common.primitives.Ints;

import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToIntFunction;

public final class IntArray {

    private IntArray() {
    }

    public static <T> List<Integer> asList(List<T> list, ToIntFunction<T> function) {
        final int size = list.size();
        final int[] values = new int[size];
        if (list instanceof RandomAccess) {
            for (int i = 0; i < size; i++) {
                values[i] = function.applyAsInt(list.get(i));
            }
        } else {
            int i = 0;
            for (T element : list) {
                values[i++] = function.applyAsInt(element);
            }
        }
        return Ints.asList(values);
    }
}
