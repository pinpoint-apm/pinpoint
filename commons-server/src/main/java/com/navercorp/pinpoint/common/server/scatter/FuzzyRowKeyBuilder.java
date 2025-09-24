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

    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    private final byte[] fuzzyRowKeyMask;
    private final byte[] fuzzyInfoMask;

    public FuzzyRowKeyBuilder(int fuzzyKeyLength) {
        fuzzyRowKeyMask = fill(fuzzyKeyLength, (byte) '?');
        fuzzyInfoMask = fill(fuzzyKeyLength, (byte) 1);
    }

    public FuzzyRowKeyBuilder() {
        this(FUZZY_KEY_LENGTH);
    }

    public static FuzzyRowKeyBuilder createBuilderV2() {
        return new FuzzyRowKeyBuilder(FUZZY_FILTER_LENGTH_V2);
    }

    private static byte[] fill(int length, byte c) {
        final byte[] bytes = new byte[length];
        Arrays.fill(bytes, c);
        bytes[bytes.length - 1] = 0;
        return bytes;
    }

    public Filter build(long yHigh, long yLow) {
        final List<Byte> keys = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return createFuzzyRowFilter(keys, fuzzyInfoMask, fuzzyRowKeyMask);
    }

    private Filter createFuzzyRowFilter(List<Byte> keys, byte[] baseFuzzyInfoMask, byte[] baseFuzzyRowKeyMask) {
        final byte[] fuzzyInfoMask = Arrays.copyOf(baseFuzzyInfoMask, baseFuzzyInfoMask.length);
        List<Pair<byte[], byte[]>> result = new ArrayList<>();
        for (Byte key : keys) {
            final byte[] fuzzyRowKey = Arrays.copyOf(baseFuzzyRowKeyMask, baseFuzzyRowKeyMask.length);
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
