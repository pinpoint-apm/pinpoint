package com.navercorp.pinpoint.common.server.util;

import org.apache.commons.codec.binary.Base16;

public final class Base16Utils {
    private static final Base16 BASE16 = new Base16(true);

    private Base16Utils() {
    }

    public static String encodeToString(byte[] bytes) {
        return BASE16.encodeToString(bytes);
    }
}
