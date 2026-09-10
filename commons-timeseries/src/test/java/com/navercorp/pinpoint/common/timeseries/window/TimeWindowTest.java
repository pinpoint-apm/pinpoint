/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.timeseries.window;

import com.google.common.collect.ImmutableList;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author emeroad
 */
class TimeWindowTest {
    private final Logger logger = LogManager.getLogger(this.getClass());


    @Test
    void testGetTimeseriesWindows() {
        TimeWindow window = new TimeWindow(Range.between(0L, 1000));
        logger.debug("{}", window.getWindowRange());
        List<Long> timeWindows = window.getTimeseriesWindows();

        assertThat(timeWindows).hasSize(0);
    }

    @Test
    void testTimestampWindows2() {
        TimeWindow window2 = new TimeWindow(Range.between(0L, TimeUnit.MINUTES.toMillis(1)));
        logger.debug("{}", window2.getWindowRange());
        List<Long> timeWindows2 = window2.getTimeseriesWindows();
        assertThat(timeWindows2).hasSize(1)
                .containsExactly(0L);
    }


    @Test
    void testTimestampWindowsSize() {
        Range range = Range.between(0L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);
        List<Long> timestamps = window.getTimeseriesWindows();
        assertEquals(1, timestamps.size());
        assertEquals(1, window.getWindowRangeCount());
    }

    @Test
    void testTimestampWindowsSize2() {
        long from = 1L;
        long to = TimeUnit.MINUTES.toMillis(1);
        Range range = Range.between(from, to);
        TimeWindow window = new TimeWindow(range);
        List<Long> timestamps = window.getTimeseriesWindows();

        int indexSize = window.getWindowIndex(to) - window.getWindowIndex(from);
        assertEquals(1, indexSize);
        assertEquals(1, timestamps.size());
        assertEquals(1, window.getWindowRangeCount());
    }

    @Test
    void testGetNextWindow_iter() {
        Range range = Range.between(1L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);

        List<Long> iter = ImmutableList.copyOf(window.iterator());
        List<Long> timestamps = window.getTimeseriesWindows();
        assertThat(timestamps)
                .isEqualTo(iter);
    }

    @Test
    void testRefineTimestamp() {

    }

    @Test
    void testGetWindowSize() {
        testWindowSize(0, TimeUnit.MINUTES.toMillis(1));
        testWindowSize(0, TimeUnit.HOURS.toMillis(1));
        testWindowSize(0, TimeUnit.HOURS.toMillis(23));
    }

    private void testWindowSize(long start, long end) {
        Range range = Range.between(start, end);
        TimeWindow window = new TimeWindow(range);

        logger.debug("{}", window.getWindowSlotSize());
    }

    @Test
    void refineRange() {
        Range range = Range.between(1L, TimeUnit.MINUTES.toMillis(1) + 1);
        TimeWindow window = new TimeWindow(range);
        Range windowRange = window.getWindowRange();
        // 1 should be replace by 0.
        logger.debug("{}", windowRange);
        assertEquals(0, windowRange.getFrom());
        assertEquals(TimeUnit.MINUTES.toMillis(1), windowRange.getTo());

    }

    @Test
    void testGetWindowRangeLength() {
        long from = 1L;
        long to = 2L;
        Range range = Range.between(from, to);
        TimeWindow window = new TimeWindow(range);
        int windowRangeLength = window.getWindowRangeCount();

        int indexSize = window.getWindowIndex(to) - window.getWindowIndex(from);
        logger.debug("indexSize={}, windowRangeLength={}", indexSize, windowRangeLength);
        assertEquals(0, windowRangeLength);

    }

    @Test
    void testGetWindowRangeLength2() {
        long from = 1L;
        long to = 1000 * 60L + 1;
        Range range = Range.between(from, to);
        TimeWindow window = new TimeWindow(range);
        int windowRangeLength = window.getWindowRangeCount();

        int indexSize = window.getWindowIndex(to) - window.getWindowIndex(from);
        logger.debug("indexSize={}, windowRangeLength={}", indexSize, windowRangeLength);
        assertEquals(1, windowRangeLength);
    }

    @Test
    void testRefineIndex1() {
        Range range = Range.between(1L, 1000 * 60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(2);
        logger.debug("{}", index);
        assertEquals(0, index);
    }

    @Test
    void testRefineIndex2() {
        Range range = Range.between(1L, 1000 * 60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(1000 * 60L);
        logger.debug("{}", index);
        assertEquals(1, index);
    }
}
