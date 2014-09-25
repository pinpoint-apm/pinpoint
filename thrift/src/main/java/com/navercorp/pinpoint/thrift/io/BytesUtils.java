package com.nhn.pinpoint.thrift.io;

/**
 * @author emeroad
 */
class BytesUtils {
    public static byte writeShort1(final short value) {
        return (byte) (value >> 8);
    }

    public static byte writeShort2(final short value) {
        return (byte) (value);
    }
}
