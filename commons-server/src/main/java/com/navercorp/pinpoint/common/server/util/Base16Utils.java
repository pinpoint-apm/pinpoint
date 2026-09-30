package com.navercorp.pinpoint.common.server.util;

import java.util.HexFormat;

/**
 * Lower-case Base16 (hex) over {@link HexFormat}. Decoding accepts either case, per RFC 4648 §8
 * ("the hexadecimal alphabet is case insensitive"), and rejects an odd-length string.
 */
public final class Base16Utils {
    private static final HexFormat HEX = HexFormat.of();

    private Base16Utils() {
    }

    public static String encodeToString(byte[] bytes) {
        return HEX.formatHex(bytes);
    }

    /**
     * @throws IllegalArgumentException if the string has an odd length or a non-hex character
     */
    public static byte[] decodeToBytes(String hex) {
        return HEX.parseHex(hex);
    }

    /**
     * Returns the full Base16 (hex) length for {@code byteCount} bytes — 2 chars per byte, returned
     * as a {@code long} so {@code byteCount * 2} never overflows for any non-negative int byteCount.
     */
    public static long encodedLength(int byteCount) {
        return (long) byteCount * 2;
    }
}
