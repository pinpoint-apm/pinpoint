package com.navercorp.pinpoint.common.util;



/**
 * copy from EncodingUtils
 * -https://github.com/apache/thrift/blob/master/lib/java/src/org/apache/thrift/EncodingUtils.java
 *
 * @author Woonduk Kang(emeroad)
 */
public final class BitFieldUtils {

    private BitFieldUtils() {
    }

    /**
     * BitField utilities.
     * Returns true if the bit at position is set in v.
     */
    public static boolean testBit(byte v, int position) {
        return testBit((int) v, position);
    }

    public static boolean testBit(short v, int position) {
        return testBit((int) v, position);
    }

    public static boolean testBit(int v, int position) {
        return (v & (1 << position)) != 0;
    }

    public static boolean testBit(long v, int position) {
        return (v & (1L << position)) != 0L;
    }

    /**
     * Returns v, with the bit at position set to zero.
     */
    public static byte clearBit(byte v, int position) {
        return (byte) clearBit((int) v, position);
    }

    public static short clearBit(short v, int position) {
        return (short) clearBit((int) v, position);
    }

    public static int clearBit(int v, int position) {
        return v & ~(1 << position);
    }

    public static long clearBit(long v, int position) {
        return v & ~(1L << position);
    }

    /**
     * Returns v, with the bit at position set to 1 or 0 depending on value.
     */
    public static byte setBit(byte v, int position, boolean value) {
        return (byte) setBit((int) v, position, value);
    }

    public static short setBit(short v, int position, boolean value) {
        return (short) setBit((int) v, position, value);
    }


    public static int setBit(int v, int position, boolean value) {
        if (value)
            return v | (1 << position);
        else
            return clearBit(v, position);
    }

    public static long setBit(long v, int position, boolean value) {
        if (value)
            return v | (1L << position);
        else
            return clearBit(v, position);
    }

    public static int getBit(byte v, int position) {
        return (v & (1 << position)) >>> position;
    }

    public static int getBit(short v, int position) {
        return (v & (1 << position)) >>> position;
    }

    public static int getBit(int v, int position) {
        return (v & (1 << position)) >>> position;
    }

    public static int getBit(long v, int position) {
        return (int)((v & (1 << position)) >>> position);
    }


    public static int get2Bit(int v, int position) {
        return getMultiBit(v, position, 2);
    }

    public static int getMultiBit(int v, int position, int bitSize) {
        int shift = position;

        int mask = 0;
        for (int i = 0; i < bitSize; i++) {
            mask |= ((v & (1 << shift)) >>> position);
            shift++;
        }
        return mask;
    }

//    TODO validate position
//    public static void checkByteBounds(int position) {
//        assert position bounds
//    }
//
//    public static void checkShortBounds(int position) {
//        assert position bounds
//    }

}
