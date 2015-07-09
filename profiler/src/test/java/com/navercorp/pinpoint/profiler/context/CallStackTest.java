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

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.navercorp.pinpoint.profiler.context.CallStack;
import com.navercorp.pinpoint.profiler.context.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class CallStackTest {
    private Span span;
    private SpanEvent spanEvent;

    @Before
    public void before() {
        span = new Span();
        spanEvent = new SpanEvent(span);
    }

    public Span createSpan() {
        DefaultTraceId traceId = new DefaultTraceId("test", 0, 1);
        Span span = new Span();
        span.setAgentId("agentId");
        span.recordTraceId(traceId);
        return span;
    }

    private SpanEvent createSpanEventStackFrame(Span span) {
        SpanEvent spanEvent = new SpanEvent(span);
        return spanEvent;
    }

    @Test
    public void testPush() throws Exception {
        CallStack callStack = new CallStack();
        int initialIndex = callStack.getIndex();
        assertEquals("initial index", initialIndex, 0);
        SpanEvent spanEvent = createSpanEventStackFrame(span);
        int index = callStack.push(spanEvent);
        assertEquals("initial index", index, 1);
        callStack.pop();
    }

    @Test
    public void testLargePush() {
        CallStack callStack = new CallStack();
        int initialIndex = callStack.getIndex();
        Assert.assertEquals("initial index", initialIndex, 0);

        final int pushCount = 32;
        for (int i = 0; i < pushCount; i++) {
            int push = callStack.push(spanEvent);
            Assert.assertEquals("push index", i + 1, push);
            int index = callStack.getIndex();
            Assert.assertEquals("index", i + 1, index);
        }
        for (int i = 0; i < pushCount - 1; i++) {
            callStack.pop();
        }
        callStack.pop();
    }

    @Test
    public void testPushPop1() {
        CallStack callStack = new CallStack();

        callStack.push(spanEvent);
        callStack.pop();
    }

    @Test
    public void testPushPop2() {
        CallStack callStack = new CallStack();

        callStack.push(spanEvent);
        callStack.push(spanEvent);

        callStack.pop();
        callStack.pop();
    }

    @Test
    public void testPop_Fail() {
        CallStack callStack = new CallStack();

        callStack.push(spanEvent);
        callStack.push(spanEvent);

        callStack.pop();
        callStack.pop();
        assertNull(callStack.pop());
    }
}