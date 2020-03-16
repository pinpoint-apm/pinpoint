package com.navercorp.pinpoint.test.plugin.util;

import java.util.Collection;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static <T> boolean hasLength(final Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }
}
