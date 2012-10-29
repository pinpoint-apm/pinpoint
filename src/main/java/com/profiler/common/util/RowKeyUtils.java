package com.profiler.common.util;

import org.apache.hadoop.hbase.util.Bytes;

/**
 *
 */
public class RowKeyUtils {

    public static int LONG_BYTE_LENGTH = 8;

    public static byte[] concatFixedByteAndLong(byte[] fixedBytes, int maxFixedLength, long l) {
        if (fixedBytes == null) {
            throw new IllegalArgumentException("fixedBytes must not null");
        }
        if (fixedBytes.length > maxFixedLength) {
            throw new IllegalArgumentException("fixedBytes.length too big. length:" + fixedBytes.length);
        }
        byte[] rowKey = new byte[maxFixedLength + LONG_BYTE_LENGTH];
        Bytes.putBytes(rowKey, 0, fixedBytes, 0, fixedBytes.length);
        BytesUtils.writeLong(l, rowKey, maxFixedLength);
        return rowKey;
    }
}
