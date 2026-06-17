package com.navercorp.pinpoint.common.server.util;

import org.apache.commons.codec.binary.Base16;

public final class Base16Utils {
    private static final Base16 BASE16 = new Base16(true);

    private Base16Utils() {
    }

    public static String encodeToString(byte[] bytes) {
        return BASE16.encodeToString(bytes);
    }

    public static byte[] decodeToBytes(String pArray) {
        return BASE16.decode(pArray);
    }

    /**
     * Returns the full Base16 (hex) length for {@code byteCount} bytes — 2 chars per byte, returned
     * as a {@code long} so {@code byteCount * 2} never overflows for any non-negative int byteCount.
     */
    public static long encodedLength(int byteCount) {
        return (long) byteCount * 2;
    }
}
