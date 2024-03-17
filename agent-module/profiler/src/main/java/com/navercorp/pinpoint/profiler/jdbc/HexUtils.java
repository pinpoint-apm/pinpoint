package com.navercorp.pinpoint.profiler.jdbc;

import java.util.Objects;

public class HexUtils {
    // for uuid
    public static final int MAX_BYTES_SIZE = 16;

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();
    private static final String ABBREV_MARKER = "...";

    private static void appendHex(StringBuilder builder, byte[] bytes, int maxSize) {
        int index = Math.min(bytes.length, maxSize);
        for (int i = 0; i < index; i++) {
            byte b = bytes[i];
            int temp = (int) b & 0xFF;
            builder.append(HEX_CHARS[temp >>> 4]);
            builder.append(HEX_CHARS[temp & 0x0F]);
        }
    }

    public static String toHexString(byte[] bytes) {
        return toHexString(bytes, MAX_BYTES_SIZE);
    }

    public static String toHexString(byte[] bytes, int maxSize) {
        Objects.requireNonNull(bytes, "bytes");

        StringBuilder builder = newBuilder(maxSize);
        appendHex(builder, bytes, maxSize);
        if (bytes.length > maxSize) {
            builder.append(ABBREV_MARKER);
        }
        return builder.toString();
    }

    private static StringBuilder newBuilder(int maxSize) {
        int capacity = (maxSize * 2) + ABBREV_MARKER.length();
        return new StringBuilder(capacity);
    }

}
