package com.navercorp.pinpoint.common.server.util.time;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RangeSplitterTest {

    @Test
    public void rangeSplitTest() {
        long start = 0;
        long end = 100;
        long fixedRange = 100;
        RangeSplitter rangeSplitter = new BackwardRangeSplitter(fixedRange);
        Range originalRange = Range.between(start, end);
        
        List<Range> result = rangeSplitter.splitRange(originalRange);

        //printRangeList(result);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void rangeSplitTest1() {
        long start = 0;
        long end = 1000;
        long fixedRange = 100;
        RangeSplitter rangeSplitter = new BackwardRangeSplitter(fixedRange);
        Range originalRange = Range.between(start, end);

        List<Range> result = rangeSplitter.splitRange(originalRange);

        //printRangeList(result);
        Assertions.assertThat(result.size()).isEqualTo(10);
    }

    @Test
    public void rangeSplitTest2() {
        long start = 0;
        long end = 7;
        long fixedRange = 2;
        long multiplier = 3;
        RangeSplitter rangeSplitter = new BackwardRangeSplitter(fixedRange, multiplier);
        Range originalRange = Range.between(start, end);

        List<Range> result = rangeSplitter.splitRange(originalRange);

        //printRangeList(result);
        Assertions.assertThat(result.size()).isEqualTo(2);
        Assertions.assertThat(result.get(0).getTo() - result.get(0).getFrom()).isEqualTo(2);
        Assertions.assertThat(result.get(1).getTo() - result.get(1).getFrom()).isEqualTo(5);
    }

    @Test
    public void rangeSplitTest3() {
        long start = 0;
        long end = 1;
        long fixedRange = 2;
        long multiplier = 3;
        RangeSplitter rangeSplitter = new BackwardRangeSplitter(fixedRange, multiplier);
        Range originalRange = Range.between(start, end);

        List<Range> result = rangeSplitter.splitRange(originalRange);

        //printRangeList(result);
        Assertions.assertThat(result.size()).isEqualTo(1);
        Assertions.assertThat(result.get(0).getTo() - result.get(0).getFrom()).isEqualTo(1);
    }

    private void printRangeList(List<Range> result) {
        for (Range range : result) {
            System.out.println(range.getFrom() + " ~ " + range.getTo());
        }
    }

}
