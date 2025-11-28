package com.navercorp.pinpoint.common.server.scatter;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

public class TraceIndexRowKey {

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    public static byte[] createFuzzyRowKey(int saltKeySize, int serviceUid, String applicationName, int serviceTypeCode,
                                           long timestamp, byte fuzzySlotKey, long spanId) {
        int applicationNameHash = hashApplicationName(applicationName);
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new AutomaticBuffer(saltKeySize + 4 + 4 + 4 + 8 + (1 + applicationName.length()) + 1 + 8);
        buffer.putPadBytes(null, saltKeySize);
        buffer.putInt(applicationNameHash);
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);
        buffer.putPrefixedString(applicationName);
        buffer.putByte(fuzzySlotKey);
        buffer.putLong(spanId);
        return buffer.getBuffer();
    }

    public static byte[] createScanRowKey(int serviceUid, String applicationName, int serviceTypeCode, long timestamp) {
        int applicationNameHash = hashApplicationName(applicationName);
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new FixedBuffer(4 + 4 + 4 + 8);
        buffer.putInt(applicationNameHash);
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);
        return buffer.getBuffer();
    }

    private static int hashApplicationName(String applicationName) {
        return hashFunction.hashUnencodedChars(applicationName).asInt();
    }
}

