package com.navercorp.pinpoint.common.timeseries.time;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvenRangeSplitterTest {
    @Test
    void bucketCount() {
        assertEquals(3, new EvenRangeSplitter(3).splitRange(randomRange()).size());
        assertEquals(4, new EvenRangeSplitter(4).splitRange(randomRange()).size());
        assertEquals(5, new EvenRangeSplitter(5).splitRange(randomRange()).size());
    }

    @Test
    void evenBucketSizes() {
        // given
        EvenRangeSplitter splitter = new EvenRangeSplitter(13);

        // when
        List<Range> ranges = splitter.splitRange(randomRange());

        // then
        List<Long> bucketSizes = ranges.stream()
                .map(range -> range.getTo() - range.getFrom() + 1)
                .toList();
        Long min = bucketSizes.stream().min(Long::compareTo).get();
        Long max = bucketSizes.stream().max(Long::compareTo).get();
        assertTrue(max - min <= 1);
    }

    @Test
    void excessiveBucketCount() {
        // given
        EvenRangeSplitter splitter = new EvenRangeSplitter(1000000);
        Range range = Range.between(0, 1);

        // when
        Executable splitJob = () -> splitter.splitRange(range);

        // then
        assertThrows(IllegalArgumentException.class, splitJob);
    }

    @Test
    void singleBucket() {
        // given
        EvenRangeSplitter splitter = new EvenRangeSplitter(1);
        Range range = randomRange();

        // when
        List<Range> ranges = splitter.splitRange(range);

        // then
        assertEquals(1, ranges.size());
        assertEquals(range, ranges.get(0));
    }

    @Test
    void noBucket() {
        // when
        Executable splitterCreation = () -> new EvenRangeSplitter(0);

        // then
        assertThrows(IllegalArgumentException.class, splitterCreation);
    }

    @Test
    void partitioned() {
        // given
        EvenRangeSplitter splitter = new EvenRangeSplitter(3);
        Range range = randomRange();

        // when
        List<Range> ranges = splitter.splitRange(range);

        // then
        long ptr = range.getFrom();
        for (Range partition : ranges) {
            assertEquals(ptr, partition.getFrom());
            ptr = partition.getTo() + 1;
        }
        assertEquals(ptr - 1, range.getTo());
    }

    @Test
    void manualTest() {
        // given
        EvenRangeSplitter splitter = new EvenRangeSplitter(2);
        Range range = Range.between(0, 1);

        // when
        List<Range> ranges = splitter.splitRange(range);

        // then
        assertEquals(2, ranges.size());
        assertEquals(Range.between(0, 0), ranges.get(0));
        assertEquals(Range.between(1, 1), ranges.get(1));
    }

    static Range randomRange() {
        return Range.unchecked(0, (long) (Math.random() * 1000000) + 100);
    }
}