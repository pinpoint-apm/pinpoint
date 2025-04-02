package com.navercorp.pinpoint.common.server.util.timewindow;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.server.util.time.Range;
import org.apache.hadoop.hbase.shaded.org.apache.curator.shaded.com.google.common.collect.Iterators;
import org.junit.jupiter.api.Assertions;
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

    @Test
    void toArray() {
        List<Long> list = new TimeSeriesVirtualList(10, 0, 1000);

        Object[] array = list.toArray();
        Long[] iter = Iterators.toArray(list.iterator(), Long.class);
        assertThat(array).isEqualTo(iter);

        // Compatibility check with java.util.List
        List<Long> arrayList = Lists.newArrayList(list.iterator());
        assertThat(array).isEqualTo(arrayList.toArray());
    }

    @Test
    void toArray_generic() {
        List<Long> list = new TimeSeriesVirtualList(10, 0, 1000);

        Long[] array = list.toArray(new Long[0]);
        Long[] iter = Iterators.toArray(list.iterator(), Long.class);
        assertThat(array).isEqualTo(iter);

        // Compatibility check with java.util.List
        List<Long> arrayList = Lists.newArrayList(list.iterator());
        assertThat(array).isEqualTo(arrayList.toArray(new Long[0]));
    }

    @Test
    void toArray_generic_copy() {
        List<Long> list = new TimeSeriesVirtualList(10, 0, 1000);

        Long[] copy = new Long[10];
        list.toArray(copy);

        Long[] iter = Iterators.toArray(list.iterator(), Long.class);
        assertThat(copy).isEqualTo(iter);

        // Compatibility check with java.util.List
        List<Long> arrayList = Lists.newArrayList(list.iterator());
        assertThat(copy).isEqualTo(arrayList.toArray());
    }


    @Test
    void unsupportedOperationException() {
        List<Long> list = new TimeSeriesVirtualList(10, 0, 1000);

        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.set(0, 100L));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.add(0, 100L));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.remove(0));
        Assertions.assertThrows(UnsupportedOperationException.class, () -> list.remove(Long.valueOf(1)));
    }
}