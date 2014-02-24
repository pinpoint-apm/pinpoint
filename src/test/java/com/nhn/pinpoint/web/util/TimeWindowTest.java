package com.nhn.pinpoint.web.util;

import com.nhn.pinpoint.web.vo.Range;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author emeroad
 */
public class TimeWindowTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Test
    public void testGetNextWindow() throws Exception {


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

        logger.debug("{}", window.getWindowSize());
    }

    @Test
    public void refineRange() {
        Range range = new Range(1L, TimeUnit.MINUTES.toMillis(1)+1);
        TimeWindow window = new TimeWindow(range);
        Range windowRange = window.createWindowRange();
        // 1은 0으로 치환 되어야 한다.
        logger.debug("{}", windowRange);
        Assert.assertEquals(windowRange.getFrom(), 0);
        Assert.assertEquals(windowRange.getTo(), TimeUnit.MINUTES.toMillis(1));

    }

    @Test
    public void testGetWindowSize1() throws Exception {

    }
}
