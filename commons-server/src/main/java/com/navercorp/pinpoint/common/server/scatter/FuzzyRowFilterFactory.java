package com.navercorp.pinpoint.common.server.scatter;

import com.navercorp.pinpoint.common.hbase.HbaseTableConstants;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FuzzyRowFilterFactory {
    private static final byte WILDCARD = 1; // 0 = exact match, other = wildcard

    private static final int traceIndexOffset = HbaseTableConstants.TRACE_INDEX_TIMESTAMP_OFFSET + 8; // timestamp(8)
    private static final int traceIndexExceedLength = 1 + 8; // fuzzyRowSlot(1) + spanId(8)

    private final int prefixLength;
    private final int exceedLength;

    public FuzzyRowFilterFactory() {
        this.prefixLength = traceIndexOffset;
        this.exceedLength = traceIndexExceedLength;
    }

    public FuzzyRowFilterFactory(int prefixLength, int exceedLength) {
        this.prefixLength = prefixLength;
        this.exceedLength = exceedLength;
    }

    public FuzzyRowFilter build(byte[] suffixPrefix) {
        int totalLength = prefixLength + suffixPrefix.length + exceedLength;

        // create mask
        byte[] mask = new byte[totalLength];
        Arrays.fill(mask, 0, prefixLength, WILDCARD);
        Arrays.fill(mask, prefixLength + suffixPrefix.length, totalLength, WILDCARD);

        // create row
        byte[] row = new byte[totalLength];
        System.arraycopy(suffixPrefix, 0, row, prefixLength, suffixPrefix.length);

        return new FuzzyRowFilter(List.of(new Pair<>(row, mask)));
    }

    public FuzzyRowFilter build(byte[] suffixPrefix,
                                List<Byte> allowedBytes) {
        int totalLength = prefixLength + suffixPrefix.length + exceedLength;
        List<Pair<byte[], byte[]>> list = new ArrayList<>();

        // create mask
        byte[] mask = new byte[totalLength];
        Arrays.fill(mask, 0, prefixLength, WILDCARD);
        Arrays.fill(mask, prefixLength + suffixPrefix.length + 1, totalLength, WILDCARD);

        // create rows
        for (byte allowed : allowedBytes) {
            byte[] row = new byte[totalLength];
            System.arraycopy(suffixPrefix, 0, row, prefixLength, suffixPrefix.length);
            row[prefixLength + suffixPrefix.length] = allowed;
            list.add(new Pair<>(row, mask));
        }

        return new FuzzyRowFilter(list);
    }

}