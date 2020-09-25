package com.navercorp.pinpoint.common.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class FilterUtils {

    public static <T> List<T> filter(Collection<?> collection, Class<T> type) {
        Objects.requireNonNull(collection, "collection");

        final List<T> result = new ArrayList<>();
        for (Object object : collection) {
            if (type.isInstance(object)) {
                @SuppressWarnings("unchecked")
                T t = (T) object;
                result.add(t);
            }
        }
        return result;
    }
}
