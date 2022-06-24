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

package com.navercorp.pinpoint.web.util;

import com.navercorp.pinpoint.common.server.util.time.Range;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowTest {
    private final Logger logger = LogManager.getLogger(this.getClass());


    @Test
    public void testGetNextWindowFirst() {
        TimeWindow window = new TimeWindow(Range.between(0L, 1000));
        logger.debug("{}", window.getWindowRange());
        Iterator<Long> iterator = window.iterator();
        Assertions.assertEquals(iterator.next(), (Long)0L);
        try {
            iterator.next();
            Assertions.fail("no more element");
        } catch (Exception ignored) {
        }

        TimeWindow window2 = new TimeWindow(Range.between(0L, TimeUnit.MINUTES.toMillis(1)));
        logger.debug("{}", window2.getWindowRange());
        Iterator<Long> iterator2 = window2.iterator();
        Assertions.assertEquals(iterator2.next(), (Long)0L);
        Assertions.assertEquals(iterator2.next(), (Long)(1000*60L));
        try {
            iterator2.next();
            Assertions.fail("no more element");
        } catch (Exception ignored) {
        }
    }

    @Test
    public void testGetNextWindow() {
        Range range = Range.between(0L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);
        int i = 0;
        for (Long aLong : window) {
            i++;
        }
        Assertions.assertEquals(i, 2);
    }

    @Test
    public void testGetNextWindow2() {
        Range range = Range.between(1L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);
        int i = 0;
        for (Long aLong : window) {
            logger.debug("{}", aLong);
            i++;
        }
        Assertions.assertEquals(i, 2);
    }

    @Test
    public void testRefineTimestamp() {

    }

    @Test
    public void testGetWindowSize() {
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
    public void refineRange() {
        Range range = Range.between(1L, TimeUnit.MINUTES.toMillis(1)+1);
        TimeWindow window = new TimeWindow(range);
        Range windowRange = window.getWindowRange();
        // 1 should be replace by 0.
        logger.debug("{}", windowRange);
        Assertions.assertEquals(windowRange.getFrom(), 0);
        Assertions.assertEquals(windowRange.getTo(), TimeUnit.MINUTES.toMillis(1));

    }

    @Test
    public void testGetWindowRangeLength() {
        Range range = Range.between(1L, 2L);
        TimeWindow window = new TimeWindow(range);
        long windowRangeLength = window.getWindowRangeCount();
        logger.debug("{}", windowRangeLength);
        Assertions.assertEquals(1, windowRangeLength);

    }

    @Test
    public void testGetWindowRangeLength2() {
        Range range = Range.between(1L, 1000*60L + 1);
        TimeWindow window = new TimeWindow(range);
        long windowRangeLength = window.getWindowRangeCount();
        logger.debug("{}", windowRangeLength);
        Assertions.assertEquals(2, windowRangeLength);
    }

    @Test
     public void testRefineIndex1() {
        Range range = Range.between(1L, 1000*60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(2);
        logger.debug("{}", index);
        Assertions.assertEquals(0, index);
    }

    @Test
     public void testRefineIndex2() {
        Range range = Range.between(1L, 1000*60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(1000 * 60L);
        logger.debug("{}", index);
        Assertions.assertEquals(1, index);
    }
}
