package com.navercorp.pinpoint.test.plugin.util;

import java.util.Collection;

public final class CollectionUtils {
    private CollectionUtils() {
    }

    public static boolean hasLength(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
