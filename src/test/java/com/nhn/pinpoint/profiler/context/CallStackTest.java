package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.context.CallStack;
import com.nhn.pinpoint.profiler.context.DefaultTraceID;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CallStackTest {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testPush() throws Exception {
        DefaultTraceID traceID = new DefaultTraceID("test", 0, 1);
        CallStack callStack = new CallStack(traceID);
        int stackIndex = callStack.getStackFrameIndex();
        logger.info(String.valueOf(stackIndex));
        callStack.push();

        callStack.popRoot();
    }

    @Test
    public void testPop() throws Exception {

    }
}