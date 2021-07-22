package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.hbase.HbaseColumnFamily;
import com.navercorp.pinpoint.common.util.BytesUtils;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FuzzyRowKeyBuilder {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int MASK_SIZE = 1; // one byte
    private static final int HBASE_SALT_KEY_SIZE = HbaseColumnFamily.APPLICATION_TRACE_INDEX_TRACE.ROW_DISTRIBUTE_SIZE; // one byte

    private static final byte[] FuzzyRowKeyMask = newFuzzyRowKey();
    private static final byte[] FuzzyInfoMask = newFuzzyInfo();


    private final FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    private static byte[] newFuzzyRowKey() {
        return fill((byte) '?');
    }

    private static byte[] newFuzzyInfo() {
        return fill((byte) 1);
    }

    private static byte[] fill(byte c) {
        final byte[] bytes = new byte[HBASE_SALT_KEY_SIZE + PinpointConstants.AGENT_ID_MAX_LEN + BytesUtils.LONG_BYTE_LENGTH + MASK_SIZE];
        Arrays.fill(bytes, c);
        bytes[bytes.length - 1] = 0;
        return bytes;
    }

    public Filter build(long yHigh, long yLow) {
        final List<Byte> keys = fuzzyRowKeyFactory.getRangeKey(yHigh, yLow);
        return newFuzzyRowFilter(keys);
    }

    private Filter newFuzzyRowFilter(List<Byte> keys) {
        final byte[] fuzzyInfoMask = Arrays.copyOf(FuzzyInfoMask, FuzzyInfoMask.length);
        List<Pair<byte[], byte[]>> result = new ArrayList<>();
        for (Byte key : keys) {
            final byte[] fuzzyRowKey = Arrays.copyOf(FuzzyRowKeyMask, FuzzyRowKeyMask.length);
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
