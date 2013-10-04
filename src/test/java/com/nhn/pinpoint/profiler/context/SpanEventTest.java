package com.nhn.pinpoint.profiler.context;

import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class SpanEventTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testMarkStartTime() throws Exception {
        final DefaultTraceId traceId = new DefaultTraceId("agentTime", 0, 0);
        Span span = new Span(traceId);
        span.markBeforeTime();
        Thread.sleep(10);
        span.markEndTime();
        logger.debug("span:{}", span);

        final SpanEvent spanEvent = new SpanEvent(span);
        spanEvent.markStartTime();
        Thread.sleep(10);
        spanEvent.markEndTime();
        logger.debug("spanEvent:{}", spanEvent);

        Assert.assertEquals("startTime", span.getStartTime() + spanEvent.getStartElapsed(), spanEvent.getStartTime());
        Assert.assertEquals("endTime", span.getStartTime() + spanEvent.getStartElapsed() + spanEvent.getEndElapsed(), spanEvent.getEndTime());
    }

    @Test
    public void testGetStartTime() throws Exception {

    }

    @Test
    public void testMarkEndTime() throws Exception {

    }

    @Test
    public void testGetEndTime() throws Exception {

    }
}
