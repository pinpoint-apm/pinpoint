package com.navercorp.pinpoint.common.timeseries.time;

import java.util.ArrayList;
import java.util.List;

public class EvenRangeSplitter implements RangeSplitter {
    private final int bucketCount;

    public EvenRangeSplitter(int bucketCount) {
        if (bucketCount < 1) {
            throw new IllegalArgumentException("bucketCount must be greater than 0");
        }

        this.bucketCount = bucketCount;
    }

    @Override
    public List<Range> splitRange(long from, long to) {
        long totalSize = to - from + 1;
        long bucketSize = totalSize / bucketCount;
        long remainder = totalSize % bucketCount;

        if (bucketSize == 0) {
            throw new IllegalArgumentException("too many buckets");
        }

        List<Range> buckets = new ArrayList<>(bucketCount);

        long start = from;
        for (int i = 0; i < bucketCount; i++) {
            long end = start + bucketSize - 1;
            if (i < remainder) {
                end++;
            }

            buckets.add(Range.unchecked(start, end));

            start = end + 1;
        }

        return buckets;
    }

    @Override
    public List<Range> splitRange(Range range) {
        return splitRange(range.getFrom(), range.getTo());
    }
}
