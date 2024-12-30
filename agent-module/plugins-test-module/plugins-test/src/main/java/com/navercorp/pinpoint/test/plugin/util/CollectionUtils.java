package com.navercorp.pinpoint.test.plugin.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static boolean hasLength(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static <T> List<T> union(List<T> list1, List<T> list2) {
        List<T> lists = new ArrayList<>(list1.size() + list2.size());
        lists.addAll(list1);
        lists.addAll(list2);
        return lists;
    }
}
