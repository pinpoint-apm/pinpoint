package com.navercorp.pinpoint.common.server.scatter;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.navercorp.pinpoint.common.buffer.Buffer;
import com.navercorp.pinpoint.common.buffer.ByteArrayUtils;
import com.navercorp.pinpoint.common.buffer.FixedBuffer;
import com.navercorp.pinpoint.common.buffer.OffsetFixedBuffer;
import com.navercorp.pinpoint.common.server.util.pair.LongPair;
import com.navercorp.pinpoint.common.timeseries.util.LongInverter;
import com.navercorp.pinpoint.common.util.BytesUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class TraceIndexRowKeyUtils {
    public static final int SALTED_ROW_TIMESTAMP_OFFSET = 1 + 4 + 4 + 4; // salt(1) + applicationNameHash(4) + serviceUid(4) + serviceType(4)
    public static final int APPLICATION_NAME_OFFSET = SALTED_ROW_TIMESTAMP_OFFSET + 8 + 8 + 4;

    private static final HashFunction hashFunction = Hashing.murmur3_32_fixed();
    private static final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    private TraceIndexRowKeyUtils() {
    }

    public static byte[] createRowKeyWithSaltSize(int saltKeySize, int serviceUid, String applicationName, int serviceTypeCode, long timestamp,
                                                  long spanId, int elapsed, int errorCode, String agentId) {
        long reverseTimestamp = LongInverter.invert(timestamp);
        byte[] applicationNameBytes = BytesUtils.toBytes(applicationName);
        Buffer buffer = new FixedBuffer(saltKeySize +
                4 + 4 + 4 + 8 +
                8 + 4 +
                BytesUtils.computeVar32ByteArraySize(applicationNameBytes)
        );
        buffer.skip(saltKeySize);
        buffer.putInt(toApplicationNameHash(applicationName));
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);

        buffer.putLong(spanId);
        buffer.putByte(toElapsedByte(elapsed));
        buffer.putByte(toErrorByte(errorCode));
        buffer.putShort(toAgentIdHash(agentId));

        buffer.putPrefixedBytes(applicationNameBytes);
        return buffer.getBuffer();
    }

    public static byte[] createScanRowKey(int serviceUid, String applicationName, int serviceTypeCode, long timestamp) {
        long reverseTimestamp = LongInverter.invert(timestamp);
        Buffer buffer = new FixedBuffer(4 + 4 + 4 + 8 +
                12
        );
        buffer.putInt(toApplicationNameHash(applicationName));
        buffer.putInt(serviceUid);
        buffer.putInt(serviceTypeCode);
        buffer.putLong(reverseTimestamp);

        buffer.putPadBytes(null, 12);  // pad 12 bytes to Prevent ArrayIndexOutOfBoundsException
        return buffer.getBuffer();
    }

    public static int toApplicationNameHash(String applicationName) {
        return hashFunction.hashUnencodedChars(applicationName).asInt();
    }

    public static byte toElapsedByte(long elapsed) {
        return fuzzyRowKeyFactory.getKey(elapsed);
    }

    public static byte toErrorByte(int errorCode) {
        return errorCode == 0 ? (byte) 0 : (byte) 1;
    } // zero or non-zero

    public static short toAgentIdHash(String agentId) {
        return (short) (hashFunction.hashUnencodedChars(agentId).asInt() >>> 16);
    }

    public static long extractAcceptTime(byte[] bytes, int offset) {
        int timestampOffset = offset + SALTED_ROW_TIMESTAMP_OFFSET;
        long reverseStartTime = ByteArrayUtils.bytesToLong(bytes, timestampOffset);
        return LongInverter.restore(reverseStartTime);
    }

    public static long extractSpanId(byte[] row, int offset) {
        int spanIdOffset = offset + SALTED_ROW_TIMESTAMP_OFFSET + 8; // timestamp(8)
        return ByteArrayUtils.bytesToLong(row, spanIdOffset);
    }

    public static String extractApplicationName(byte[] bytes, int offset) {
        int timestampOffset = offset + APPLICATION_NAME_OFFSET;
        Buffer buffer = new OffsetFixedBuffer(bytes);
        buffer.setOffset(timestampOffset);
        return buffer.readPrefixedString();
    }

    public static Predicate<byte[]> createApplicationNamePredicate(String applicationName) {
        Buffer buffer = new FixedBuffer(BytesUtils.computeVar32StringSize(applicationName));
        buffer.putPrefixedString(applicationName);
        byte[] prefixedApplicationName = buffer.getBuffer();

        return row -> {
            if (row.length < APPLICATION_NAME_OFFSET) {
                return false;
            } else {
                return Arrays.equals(row, APPLICATION_NAME_OFFSET, row.length,
                        prefixedApplicationName, 0, prefixedApplicationName.length);
            }
        };
    }
}

