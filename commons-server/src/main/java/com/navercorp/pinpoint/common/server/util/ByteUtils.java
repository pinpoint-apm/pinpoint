package com.navercorp.pinpoint.common.server.util;

public final class ByteUtils {
    public static final int UNSIGNED_BYTE_MIN_VALUE = 0;
    public static final int UNSIGNED_BYTE_MAX_VALUE = 255;

    private ByteUtils() {
    }

    /**
     * Range : 0 ~ 255
     */
    public static byte toUnsignedByte(int value) {
        if (value < UNSIGNED_BYTE_MIN_VALUE || value > UNSIGNED_BYTE_MAX_VALUE) {
            throw new IllegalArgumentException("UnsignedByte Out of Range (0~255)");
        }
        return (byte) (value & 0xff);
    }
}
