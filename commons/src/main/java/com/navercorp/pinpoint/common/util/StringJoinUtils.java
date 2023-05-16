package com.navercorp.pinpoint.common.util;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Objects;
import java.util.StringJoiner;

public class StringJoinUtils {
    private StringJoinUtils() {
    }

    public static <T> String join(final Collection<String> collection, final String delimiter) {
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
        StringJoiner joiner = new StringJoiner(delimiter);
        for (String str : collection) {
            joiner.add(str);
        }
        return joiner.toString();
    }

    private static String getFirstElement(Collection<String> collection) {
        final Iterator<String> iterator = collection.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            throw new ConcurrentModificationException("size:" + collection.size());
        }
    }

}
