package com.navercorp.pinpoint.test.plugin.util;

import java.util.Map;

public final class MapUtils {

    public static boolean isEmpty(final Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean hasLength(final Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }
}
