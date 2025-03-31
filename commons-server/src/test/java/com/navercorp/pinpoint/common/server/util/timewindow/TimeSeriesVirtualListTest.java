package com.navercorp.pinpoint.common.server.util.timewindow;

import com.google.common.collect.ImmutableList;
import com.navercorp.pinpoint.common.server.util.time.Range;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class TimeSeriesVirtualListTest {

    @Test
    void list() {
        TimeWindowSampler sampler = new FixedTimeWindowSampler(100);
        TimeWindow timeWindow = new TimeWindow(Range.between(0L, 10000L), sampler);
        List<Long> timeWindowList = ImmutableList.copyOf(timeWindow.iterator());

        List<Long> list = new TimeSeriesVirtualList(timeWindow.getWindowRangeCount(), 0, timeWindow.getWindowSlotSize());

        assertThat(list).isEqualTo(timeWindowList);
    }
}