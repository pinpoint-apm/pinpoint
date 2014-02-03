package com.nhn.pinpoint.profiler.context;

import org.junit.Ignore;
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
    public void testPushPop1() throws Exception {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.popRoot();

    }

    @Test
    public void testPushPop2() throws Exception {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.push();

        callStack.pop();
        callStack.popRoot();

    }

    @Ignore
    @Test
    public void testPop_Fail() throws Exception {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.push();

        callStack.pop();

        callStack.pop();
        // pop일때의 인덱스 조정을 다시 생각해 보는게 좋을듯하다.
    }
}