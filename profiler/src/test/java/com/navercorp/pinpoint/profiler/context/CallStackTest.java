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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public class CallStackTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private Span span;
    private SpanEvent spanEvent;

    @Before
    public void before() {
        span = new Span();
        spanEvent = new SpanEvent(span);
    }

    private SpanEvent createSpanEventStackFrame(Span span) {
        SpanEvent spanEvent = new SpanEvent(span);
        return spanEvent;
    }

    @Test
    public void testPush() throws Exception {
        CallStack callStack = new CallStack(span);
        int initialIndex = callStack.getIndex();
        assertEquals("initial index", initialIndex, 0);
        SpanEvent spanEvent = createSpanEventStackFrame(span);
        int index = callStack.push(spanEvent);
        assertEquals("initial index", index, 1);
        callStack.pop();
    }

    @Test
    public void testLargePush() {
        CallStack callStack = new CallStack(span);
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
        CallStack callStack = new CallStack(span);

        callStack.push(spanEvent);
        callStack.pop();
    }

    @Test
    public void testPushPop2() {
        CallStack callStack = new CallStack(span);

        callStack.push(spanEvent);
        callStack.push(spanEvent);

        callStack.pop();
        callStack.pop();
    }

    @Test
    public void testPop_Fail() {
        CallStack callStack = new CallStack(span);

        callStack.push(spanEvent);
        callStack.push(spanEvent);

        callStack.pop();
        callStack.pop();
        assertNull(callStack.pop());
    }
    
    @Test
    public void overflow() {
        final int maxDepth = 3;
        
        CallStack callStack = new CallStack(span, maxDepth);
        assertEquals(maxDepth, callStack.getMaxDepth());
        
        for(int i = 0; i < maxDepth; i++) {
            assertEquals(i + 1, callStack.push(spanEvent));
        }
        // overflow
        int overflowIndex = callStack.push(spanEvent);
        assertEquals(maxDepth + 1, overflowIndex);
        assertEquals(maxDepth + 1, callStack.getIndex());
        assertTrue(callStack.isOverflow());
        assertNotNull(callStack.peek());
        // check inner index value.
        logger.debug("{}", callStack);

        assertNotNull(callStack.pop());
        assertEquals(maxDepth, callStack.getIndex());
        
        // normal
        for(int i = maxDepth; i > 0; i--) {
            assertNotNull(callStack.peek());
            assertNotNull(callStack.pop());
            assertEquals(i - 1, callStack.getIndex());
        }
        
        // low overflow
        assertNull(callStack.pop());
        assertNull(callStack.peek());
    }
}