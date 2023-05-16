package com.navercorp.pinpoint.web.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ListListUtils {
    private ListListUtils() {
    }

    public static <T> List<T> toList(List<List<T>> listList, int initialCapacity) {
        Objects.requireNonNull(listList, "listList");
        final List<T> sum = new ArrayList<>(initialCapacity);
        for (List<T> list : listList) {
            sum.addAll(list);
        }
        return sum;
    }

    public static <T> List<T> toList(List<List<T>> listList) {
        return toList(listList, getSize(listList));
    }

    private static <T> int getSize(List<List<T>> listList) {
        int size = 0;
        for (List<T> list : listList) {
            size += list.size();
        }
        return size;
    }

}
