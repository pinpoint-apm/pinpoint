package com.profiler.common.util;

public class BytesUtils {
    public static final byte[] longLongToBytes(long value1, long value2) {
        byte[] buffer = new byte[16];
        writeLong(value1, buffer, 0);
        writeLong(value2, buffer, 8);
        return buffer;
    }

	public static final long[] bytesToLongLong(byte[] b) {
		long[] result = new long[2];

		result[0] = byteToLong(b, 0);
		result[1] = byteToLong(b, 8);

		return result;
	}

	public static final long byteToLong(byte[] b, int offset) {
		if (b.length < offset + 8) {
			throw new IllegalArgumentException("Illegal bytes or offset.");
		}

		long rv = 0;
		for (int index = offset; index < offset + 8; index++) {
			byte i = b[index];
			rv = (rv << 8) | (i < 0 ? 256 + i : i);
		}

		return rv;
	}
    
    private static void writeLong(long value, byte[] buf, int offset) {
//        for (int i = offset + 7; i > offset; i--) {
//          buf [i]= (byte) val;
//          val >>>= 8;
//        }
//        buf[offset] = (byte) val;

        buf[offset++] = (byte) (value >> 56);
		buf[offset++] = (byte) (value >> 48);
		buf[offset++] = (byte) (value >> 40);
		buf[offset++] = (byte) (value >> 32);
		buf[offset++] = (byte) (value >> 24);
		buf[offset++] = (byte) (value >> 16);
		buf[offset++] = (byte) (value >> 8);
		buf[offset] = (byte) (value);
    }
}
