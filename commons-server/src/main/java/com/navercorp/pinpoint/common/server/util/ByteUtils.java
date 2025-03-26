package com.navercorp.pinpoint.common.server.util;

public final class ByteUtils {

    private ByteUtils() {
    }

    /**
     * Range : 0 ~ 255
     */
    public static byte toUnsignedByte(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("UnsignedByte Out of Range (0~255)");
        }
        return (byte) (value & 0xff);
    }
}
