package com.navercorp.pinpoint.common.server.util.time;

import java.util.ArrayList;
import java.util.List;

public class BackwardRangeSplitter implements RangeSplitter {

    private final static long DAY_MILLIS = 86400000L;

    private final long rangeSizeMillis;
    private final long multiplier;

    public BackwardRangeSplitter() {
        this(DAY_MILLIS * 2, 3);
    }

    public BackwardRangeSplitter(long rangeSizeMillis) {
        this(rangeSizeMillis, 1);
    }

    public BackwardRangeSplitter(long rangeSizeMillis, long multiplier) {
        this.rangeSizeMillis = rangeSizeMillis;
        this.multiplier = multiplier;
    }

    @Override
    public List<Range> splitRange(long from, long to) {
        List<Range> ranges = new ArrayList<>();
        long splitSize = rangeSizeMillis;
        long splitEnd = to;
        long splitStart = splitEnd - splitSize;
        while (from < splitStart) {
            ranges.add(Range.between(splitStart , splitEnd));

            splitSize *= multiplier;
            splitEnd = splitStart;
            splitStart = splitEnd - splitSize;
        }

        ranges.add(Range.between(from, splitEnd));

        return ranges;
    }

    @Override
    public List<Range> splitRange(Range range) {
        return splitRange(range.getFrom(), range.getTo());
    }
}
