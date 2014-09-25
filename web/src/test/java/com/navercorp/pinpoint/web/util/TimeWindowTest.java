package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testGetNextWindowFirst() throws Exception {
        TimeWindow window = new TimeWindow(new Range(0L, 1000));
        logger.debug("{}", window.getWindowRange());
        Iterator<Long> iterator = window.iterator();
        Assert.assertEquals(iterator.next(), (Long)0L);
        try {
            iterator.next();
            Assert.fail("no more element");
        } catch (Exception e) {
        }

        TimeWindow window2 = new TimeWindow(new Range(0L, TimeUnit.MINUTES.toMillis(1)));
        logger.debug("{}", window2.getWindowRange());
        Iterator<Long> iterator2 = window2.iterator();
        Assert.assertEquals(iterator2.next(), (Long)0L);
        Assert.assertEquals(iterator2.next(), (Long)(1000*60L));
        try {
            iterator2.next();
            Assert.fail("no more element");
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetNextWindow() throws Exception {
        Range range = new Range(0L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);
        int i = 0;
        for (Long aLong : window) {
            i++;
        }
        Assert.assertEquals(i, 2);
    }

    @Test
    public void testGetNextWindow2() throws Exception {
        Range range = new Range(1L, TimeUnit.MINUTES.toMillis(1));
        TimeWindow window = new TimeWindow(range);
        int i = 0;
        for (Long aLong : window) {
            logger.debug("{}", aLong);
            i++;
        }
        Assert.assertEquals(i, 2);
    }

    @Test
    public void testRefineTimestamp() throws Exception {

    }

    @Test
    public void testGetWindowSize() throws Exception {
        testWindowSize(0, TimeUnit.MINUTES.toMillis(1));
        testWindowSize(0, TimeUnit.HOURS.toMillis(1));
        testWindowSize(0, TimeUnit.HOURS.toMillis(23));
    }

    private void testWindowSize(long start, long end) {
        Range range = new Range(start, end);
        TimeWindow window = new TimeWindow(range);

        logger.debug("{}", window.getWindowSlotSize());
    }

    @Test
    public void refineRange() {
        Range range = new Range(1L, TimeUnit.MINUTES.toMillis(1)+1);
        TimeWindow window = new TimeWindow(range);
        Range windowRange = window.getWindowRange();
        // 1은 0으로 치환 되어야 한다.
        logger.debug("{}", windowRange);
        Assert.assertEquals(windowRange.getFrom(), 0);
        Assert.assertEquals(windowRange.getTo(), TimeUnit.MINUTES.toMillis(1));

    }

    @Test
    public void testGetWindowRangeLength() throws Exception {
        Range range = new Range(1L, 2L);
        TimeWindow window = new TimeWindow(range);
        long windowRangeLength = window.getWindowRangeCount();
        logger.debug("{}", windowRangeLength);
        Assert.assertEquals(1, windowRangeLength);

    }

    @Test
    public void testGetWindowRangeLength2() throws Exception {
        Range range = new Range(1L, 1000*60L + 1);
        TimeWindow window = new TimeWindow(range);
        long windowRangeLength = window.getWindowRangeCount();
        logger.debug("{}", windowRangeLength);
        Assert.assertEquals(2, windowRangeLength);
    }

    @Test
     public void testRefineIndex1() throws Exception {
        Range range = new Range(1L, 1000*60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(2);
        logger.debug("{}", index);
        Assert.assertEquals(0, index);
    }

    @Test
     public void testRefineIndex2() throws Exception {
        Range range = new Range(1L, 1000*60L);
        TimeWindow window = new TimeWindow(range);
        long index = window.getWindowIndex(1000 * 60L);
        logger.debug("{}", index);
        Assert.assertEquals(1, index);
    }
}
