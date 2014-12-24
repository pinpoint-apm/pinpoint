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

package com.navercorp.pinpoint.profiler.context;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.context.CallStack;
import com.navercorp.pinpoint.profiler.context.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.SpanEventStackFrame;
import com.navercorp.pinpoint.profiler.context.StackFrame;

/**
 * @author emeroad
 */
public class CallStackTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Span createSpan() {
        DefaultTraceId traceId = new DefaultTraceId("test", 0, 1);
        Span span = new Span();
        span.setAgentId("agentId");
        span.recordTraceId(traceId);
        return span;
    }

    private SpanEventStackFrame createSpanEventStackFrame(Span span) {
        SpanEvent spanEvent = new SpanEvent(span);
        SpanEventStackFrame spanEventStackFrame = new SpanEventStackFrame(spanEvent);
        return spanEventStackFrame;
    }


    @Test
    public void testPush() throws Exception {
        final Span span = createSpan();

        CallStack callStack = new CallStack(span);
        int initialIndex = callStack.getStackFrameIndex();
        Assert.assertEquals("initial index", initialIndex, -1);

        callStack.push();

        SpanEventStackFrame spanEventStackFrame = createSpanEventStackFrame(span);
        callStack.setStackFrame(spanEventStackFrame);

        callStack.popRoot();
    }

    @Test
    public void testLargePush() {
        final Span span = createSpan();

        CallStack callStack = new CallStack(span);
        int initialIndex = callStack.getStackFrameIndex();
        Assert.assertEquals("initial index", initialIndex, -1);

        final int pushCount = 32;
        for(int i = 0; i< pushCount; i++) {
            int push = callStack.push();
            Assert.assertEquals("push index", i, push);

            SpanEventStackFrame stackFrame = createSpanEventStackFrame(span);
            callStack.setStackFrame(stackFrame);

            int index = callStack.getIndex();
            Assert.assertEquals("index", i, index);
        }
        for(int i = 0; i< pushCount-1; i++) {
            callStack.pop();
        }
        callStack.popRoot();
    }



    @Test
    public void testPushPop1() {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.popRoot();

    }

    @Test
    public void testPushPop2() {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.push();

        callStack.pop();
        callStack.popRoot();

    }

    @Test
    public void testRootPop_fail() {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.push();

        try {
            callStack.popRoot();
            Assert.fail("invalid popRoot");
        } catch (Exception e) {
        }

    }

    @Test
    public void testPop_Fail() {
        CallStack callStack = new CallStack(new Span());

        callStack.push();
        callStack.push();

        callStack.pop();

        StackFrame lastPop = callStack.pop();
        Assert.assertNull(lastPop);

        try {
            callStack.pop();
            Assert.fail("invalid pop");
        } catch (Exception e) {
        }
    }
}