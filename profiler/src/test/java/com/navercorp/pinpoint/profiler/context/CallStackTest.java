/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public abstract class CallStackTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    protected CallStack.Factory<SpanEvent> factory = new SpanEventFactory();

    abstract CallStack<SpanEvent> newCallStack();

    abstract CallStack<SpanEvent> newCallStack(int depth);

    abstract CallStack<SpanEvent> newCallStack(int depth, int sequence);

    public SpanEvent getSpanEvent() {
        return factory.newInstance();
    }

    @Test
    public void testPush() {
        CallStack<SpanEvent> callStack = newCallStack();
        int initialIndex = callStack.getIndex();
        assertEquals(initialIndex, 0, "initial index");
        SpanEvent spanEvent = factory.newInstance();
        int index = callStack.push(spanEvent);
        assertEquals(index, 1, "initial index");
        callStack.pop();
    }


    @Test
    public void testLargePush() {
        CallStack<SpanEvent> callStack = newCallStack();
        int initialIndex = callStack.getIndex();
        assertEquals(initialIndex, 0, "initial index");

        final int pushCount = 32;
        for (int i = 0; i < pushCount; i++) {
            int push = callStack.push(getSpanEvent());
            assertEquals(i + 1, push, "push index");
            int index = callStack.getIndex();
            assertEquals(i + 1, index, "index");
        }
        for (int i = 0; i < pushCount - 1; i++) {
            callStack.pop();
        }
        callStack.pop();
    }

    @Test
    public void testPushPop1() {
        CallStack<SpanEvent> callStack = newCallStack();

        callStack.push(getSpanEvent());
        callStack.pop();
    }

    @Test
    public void testPushPop2() {
        CallStack<SpanEvent> callStack = newCallStack();

        callStack.push(getSpanEvent());
        callStack.push(getSpanEvent());

        callStack.pop();
        callStack.pop();
    }

    @Test
    public void testPop_Fail() {
        CallStack<SpanEvent> callStack = newCallStack();

        callStack.push(getSpanEvent());
        callStack.push(getSpanEvent());

        callStack.pop();
        callStack.pop();
        assertNull(callStack.pop());
    }

    @Test
    public void newInstance() {
        CallStack<SpanEvent> callStack = newCallStack(0);
        SpanEvent spanEvent1 = callStack.newInstance();
        callStack.push(spanEvent1);

        SpanEvent spanEvent2 = callStack.newInstance();
        assertTrue(callStack.getFactory().isDisable(spanEvent2));
    }

    @Test
    public void overflow() {
        final int maxDepth = 3;

        DefaultCallStack<SpanEvent> callStack = (DefaultCallStack<SpanEvent>) newCallStack(maxDepth);
        assertEquals(maxDepth, callStack.getMaxDepth());

        for (int i = 0; i < maxDepth; i++) {
            assertEquals(i + 1, callStack.push(getSpanEvent()));
        }
        // overflow
        int overflowIndex = callStack.push(getSpanEvent());
        assertEquals(maxDepth + 1, overflowIndex);
        assertEquals(maxDepth + 1, callStack.getIndex());

        assertTrue(callStack.isOverflow());
        assertNotNull(callStack.peek());
        // check inner index value.
        logger.debug("{}", callStack);

        assertNotNull(callStack.pop());
        assertEquals(maxDepth, callStack.getIndex());

        // normal
        for (int i = maxDepth; i > 0; i--) {
            assertNotNull(callStack.peek());
            assertNotNull(callStack.pop());
            assertEquals(i - 1, callStack.getIndex());
        }

        // low overflow
        assertNull(callStack.pop());
        assertNull(callStack.peek());
    }

    @Test
    public void overflow2() {
        final int maxDepth = 4;
        final int maxSequence = maxDepth * 2;

        DefaultCallStack<SpanEvent> callStack = (DefaultCallStack<SpanEvent>) newCallStack(maxDepth, maxSequence);
        assertEquals(maxDepth, callStack.getMaxDepth());
        assertEquals(maxSequence, callStack.getMaxSequence());

        for (int i = 0; i < maxDepth + 3; i++) {
            callStack.push(getSpanEvent());
        }

        for (int i = 0; i < maxDepth + 3; i++) {
            callStack.pop();
        }

        for (int i = 0; i < maxDepth - 1; i++) {
            callStack.push(getSpanEvent());
        }

        // overflow by sequence
        assertEquals(maxDepth - 1, callStack.getIndex());
        assertTrue(callStack.isOverflow());
        assertFalse(callStack.getFactory().isDisable(callStack.peek()));

        callStack.push(getSpanEvent());
        assertEquals(maxDepth, callStack.getIndex());
        assertTrue(callStack.getFactory().isDisable(callStack.peek()));
        assertTrue(callStack.getFactory().isDisable(callStack.pop()));

        for (int i = maxDepth - 1; i > 0; i--) {
            assertNotNull(callStack.peek());
            assertFalse(callStack.getFactory().isDisable(callStack.peek()));
            SpanEvent element = callStack.pop();
            assertNotNull(element);
            assertFalse(callStack.getFactory().isDisable(element));
            assertTrue(callStack.isOverflow());
        }
    }
}