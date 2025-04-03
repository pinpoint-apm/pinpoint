package com.navercorp.pinpoint.common.timeseries.array;

import com.google.common.primitives.Doubles;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.ToDoubleFunction;

public final class DoubleArray {

    private DoubleArray() {
    }

    public static double[] newArray(int size, double defaultValue) {
        double[] array = new double[size];
        Arrays.fill(array, defaultValue);
        return array;
    }

    public static <T> double[] asDoubleArray(List<T> list, ToDoubleFunction<T> function) {
        if (list instanceof RandomAccess) {
            final int size = list.size();
            final double[] values = new double[size];
            for (int i = 0; i < size; i++) {
                values[i] = function.applyAsDouble(list.get(i));
            }
            return values;
        }
        return asDoubleArray((Collection<T>) list, function);
    }

    public static <T> double[] asDoubleArray(Collection<T> list, ToDoubleFunction<T> function) {
        final int size = list.size();
        final double[] values = new double[size];
        int i = 0;
        for (T element : list) {
            values[i++] = function.applyAsDouble(element);
        }
        return values;
    }

    public static <T> List<Double> asList(List<T> list, ToDoubleFunction<T> function) {
        return Doubles.asList(asDoubleArray(list, function));
    }

}
