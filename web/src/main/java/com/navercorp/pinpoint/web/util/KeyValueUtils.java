package com.nhn.pinpoint.web.util;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author emeroad
 */
public class KeyValueUtils {

    public static boolean equalsFamily(KeyValue keyValue, byte[] familyName) {
        if (keyValue == null) {
            throw new NullPointerException("keyValue must not be null");
        }
        if (familyName == null) {
            throw new NullPointerException("familyName must not be null");
        }
        final byte[] buffer = keyValue.getBuffer();
        final int familyOffset = keyValue.getFamilyOffset();
        final byte familyLength = keyValue.getFamilyLength(familyOffset);
        return Bytes.equals(buffer, familyOffset, familyLength, familyName, 0, familyName.length);
    }
}
