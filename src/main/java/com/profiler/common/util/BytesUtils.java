package com.profiler.common.util;


public class BytesUtils {

    public static byte[] longLongToBytes(long value1, long value2) {
        byte[] buffer = new byte[16];
        writeFirstLong(value1, buffer);
        writeSecondLong(value2, buffer);
        return buffer;
    }

    public static long[] bytesToLongLong(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("Illegal buf size.");
        }
        long[] result = new long[2];

        result[0] = bytesToFirstLong(buf);
        result[1] = bytesToSecondLong(buf);

        return result;
    }

    public static long bytesToLong(byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 8) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + offset + 8);
        }

        long rv = (((long) buf[offset] & 0xff) << 56)
                | (((long) buf[offset + 1] & 0xff) << 48)
                | (((long) buf[offset + 2] & 0xff) << 40)
                | (((long) buf[offset + 3] & 0xff) << 32)
                | (((long) buf[offset + 4] & 0xff) << 24)
                | (((long) buf[offset + 5] & 0xff) << 16)
                | (((long) buf[offset + 6] & 0xff) << 8)
                | (((long) buf[offset + 7] & 0xff));
        return rv;
    }

    public static long bytesToFirstLong(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 8) {
            throw new IllegalArgumentException("buf.length is too small(8). buf.length:" + buf.length);
        }

        long rv = (((long) buf[0] & 0xff) << 56)
                | (((long) buf[1] & 0xff) << 48)
                | (((long) buf[2] & 0xff) << 40)
                | (((long) buf[3] & 0xff) << 32)
                | (((long) buf[4] & 0xff) << 24)
                | (((long) buf[5] & 0xff) << 16)
                | (((long) buf[6] & 0xff) << 8)
                | (((long) buf[7] & 0xff));
        return rv;
    }

    public static long bytesToSecondLong(byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("buf.length is too small(16). buf.length:" + buf.length);
        }

        long rv = (((long) buf[8] & 0xff) << 56)
                | (((long) buf[9] & 0xff) << 48)
                | (((long) buf[10] & 0xff) << 40)
                | (((long) buf[11] & 0xff) << 32)
                | (((long) buf[12] & 0xff) << 24)
                | (((long) buf[13] & 0xff) << 16)
                | (((long) buf[14] & 0xff) << 8)
                | (((long) buf[15] & 0xff));
        return rv;
    }

    public static void writeLong(long value, byte[] buf, int offset) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < offset + 8) {
            throw new IllegalArgumentException("buf.length is too small. buf.length:" + buf.length + " offset:" + offset + 8);
        }
        buf[offset++] = (byte) (value >> 56);
        buf[offset++] = (byte) (value >> 48);
        buf[offset++] = (byte) (value >> 40);
        buf[offset++] = (byte) (value >> 32);
        buf[offset++] = (byte) (value >> 24);
        buf[offset++] = (byte) (value >> 16);
        buf[offset++] = (byte) (value >> 8);
        buf[offset] = (byte) (value);
    }

    public static void writeFirstLong(long value, byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 8) {
            throw new IllegalArgumentException("buf.length is too small(8). buf.length:" + buf.length);
        }
        buf[0] = (byte) (value >> 56);
        buf[1] = (byte) (value >> 48);
        buf[2] = (byte) (value >> 40);
        buf[3] = (byte) (value >> 32);
        buf[4] = (byte) (value >> 24);
        buf[5] = (byte) (value >> 16);
        buf[6] = (byte) (value >> 8);
        buf[7] = (byte) (value);
    }

    public static void writeSecondLong(long value, byte[] buf) {
        if (buf == null) {
            throw new NullPointerException("buf must not be null");
        }
        if (buf.length < 16) {
            throw new IllegalArgumentException("buf.length is too small(16). buf.length:" + buf.length);
        }
        buf[8] = (byte) (value >> 56);
        buf[9] = (byte) (value >> 48);
        buf[10] = (byte) (value >> 40);
        buf[11] = (byte) (value >> 32);
        buf[12] = (byte) (value >> 24);
        buf[13] = (byte) (value >> 16);
        buf[14] = (byte) (value >> 8);
        buf[15] = (byte) (value);
    }
}
