package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import com.navercorp.pinpoint.common.hbase.HbaseTables;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FuzzyRowKeyBuilder {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int MASK_SIZE = 1; // one byte
    private static final int HBASE_SALT_KEY_SIZE = HbaseTables.ApplicationTraceIndexTrace.ROW_DISTRIBUTE_SIZE; // one byte

    private static final int FUZZY_KEY_LENGTH = HBASE_SALT_KEY_SIZE + PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + MASK_SIZE;
    private static final int FUZZY_FILTER_LENGTH_V2 = HbaseTableConstants.TRACE_INDEX_SALT_KEY_SIZE + HbaseTableConstants.TRACE_INDEX_ROW_KEY_SIZE + MASK_SIZE;

    private static final byte[] FuzzyRowKeyMask = newFuzzyRowKey();
    private static final byte[] FuzzyInfoMask = newFuzzyInfo();

    private static final byte[] FuzzyRowKeyMaskV2 = newFuzzyRowKeyV2();
    private static final byte[] FuzzyInfoMaskV2 = newFuzzyInfoV2();


    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    private static byte[] newFuzzyRowKey() {
        return fill(FUZZY_KEY_LENGTH, (byte) '?');
    }
    private static byte[] newFuzzyInfo() {
        return fill(FUZZY_KEY_LENGTH, (byte) 1);
    }

    private static byte[] newFuzzyRowKeyV2() {
        return fill(FUZZY_FILTER_LENGTH_V2, (byte) '?');
    }
    private static byte[] newFuzzyInfoV2() {
        return fill(FUZZY_FILTER_LENGTH_V2, (byte) 1);
    }

    private static byte[] fill(int length, byte c) {
        final byte[] bytes = new byte[length];
        Arrays.fill(bytes, c);
        bytes[bytes.length - 1] = 0;
        return bytes;
    }

    public Filter build(long yHigh, long yLow) {
        final List<Byte> keys = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return createFuzzyRowFilter(keys, FuzzyInfoMask, FuzzyRowKeyMask);
    }

    public Filter buildV2(long yHigh, long yLow) {
        final List<Byte> keys = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return createFuzzyRowFilter(keys, FuzzyInfoMaskV2, FuzzyRowKeyMaskV2);
    }

    private Filter createFuzzyRowFilter(List<Byte> keys, byte[] staticFuzzyInfoMask, byte[] staticFuzzyRowKeyMask) {
        final byte[] fuzzyInfoMask = Arrays.copyOf(staticFuzzyInfoMask, staticFuzzyInfoMask.length);
        List<Pair<byte[], byte[]>> result = new ArrayList<>();
        for (Byte key : keys) {
            final byte[] fuzzyRowKey = Arrays.copyOf(staticFuzzyRowKeyMask, staticFuzzyRowKeyMask.length);
            fuzzyRowKey[fuzzyInfoMask.length - 1] = key;
            Pair<byte[], byte[]> fuzzyPair = new Pair<>(fuzzyRowKey, fuzzyInfoMask);
            if (logger.isTraceEnabled()) {
                logger.trace("fuzzy rowkey:{} info:{} {}/{}",
                        Bytes.toStringBinary(fuzzyRowKey), Bytes.toStringBinary(fuzzyInfoMask), fuzzyRowKey.length, fuzzyInfoMask.length);
            }
            result.add(fuzzyPair);
        }
        return new FuzzyRowFilter(result);
    }

}
