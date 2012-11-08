package com.profiler.context;

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
        CallStack callStack = new CallStack();
        int stackIndex = callStack.getStackFrameIndex();
        logger.info(String.valueOf(stackIndex));

    }

    @Test
    public void testPop() throws Exception {

    }
}