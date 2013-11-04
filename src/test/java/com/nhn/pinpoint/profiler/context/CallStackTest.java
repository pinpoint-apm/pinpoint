package com.nhn.pinpoint.profiler.context;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 */
public class CallStackTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testPush() throws Exception {
        DefaultTraceId traceId = new DefaultTraceId("test", 0, 1);
        Span span = new Span();
        span.setAgentId("agentId");
        span.recordTraceId(traceId);
        CallStack callStack = new CallStack(span);
        int stackIndex = callStack.getStackFrameIndex();
        logger.info(String.valueOf(stackIndex));
        callStack.push();

        callStack.popRoot();
    }

    @Test
    public void testPop() throws Exception {

    }
}