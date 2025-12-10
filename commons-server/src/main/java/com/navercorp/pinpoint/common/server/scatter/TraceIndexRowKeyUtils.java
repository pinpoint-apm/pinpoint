package com.navercorp.pinpoint.common.server.scatter;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.buffer.AutomaticBuffer;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;

public class TraceIndexRowKeyUtils {

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();

    private TraceIndexRowKeyUtils() {
    }

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

    public static long extractAcceptTime(byte[] bytes, int offset, int length) {
        int timestampOffset = offset + HbaseTableConstants.TRACE_INDEX_TIMESTAMP_OFFSET;
        long reverseStartTime = ByteArrayUtils.bytesToLong(bytes, timestampOffset);
        return LongInverter.restore(reverseStartTime);
    }

    public static long extractSpanId(byte[] row, int offset, int length) {
        return ByteArrayUtils.bytesToLong(row, offset + length - ByteArrayUtils.LONG_BYTE_LENGTH);
    }
}

