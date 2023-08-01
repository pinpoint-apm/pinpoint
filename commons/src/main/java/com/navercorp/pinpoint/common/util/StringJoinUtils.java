package com.navercorp.pinpoint.common.util;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class StringJoinUtils {
    private StringJoinUtils() {
    }

    public static String join(final Collection<String> collection, final String delimiter) {
        if (collection == null) {
            return null;
        }
        Objects.requireNonNull(delimiter, "delimiter");

        final int size = collection.size();
        if (size == 0) {
            return "";
        }
        if (size == 1) {
            return getFirstElement(collection);
        }
        return String.join(delimiter, collection);
    }

    private static String getFirstElement(Collection<String> collection) {
        if (collection instanceof List) {
            List<String> list = (List<String>) collection;
            return list.get(0);
        } else {
            final Iterator<String> iterator = collection.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            } else {
                throw new ConcurrentModificationException("size:" + collection.size());
            }
        }
    }

}
