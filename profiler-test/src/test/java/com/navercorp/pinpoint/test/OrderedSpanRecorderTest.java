/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.thrift.TBase;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;

/**
 * @author HyunGil Jeong
 */
public class OrderedSpanRecorderTest {
    
    private static final int UNSET_ASYNC_ID = 0;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final OrderedSpanRecorder recorder = new OrderedSpanRecorder();

    @After
    public void tearDown() throws Exception {
        this.recorder.clear();
    }

    @Test
    public void testOrderingWithSameEventTime() {
        // given
        final long startTime = System.currentTimeMillis();
        final long spanId = 1L;
        Span span = createSpan(startTime, spanId);
        SpanEvent event = createSpanEvent(span, 0, (short)0);
        SpanEvent event1 = createSpanEvent(span, 0, (short)1);
        SpanEvent event2 = createSpanEvent(span, 0, (short)2);
        SpanEvent asyncEvent1 = createAsyncSpanEvent(span, 0, (short)0, 1);
        SpanEvent asyncEvent1_1 = createAsyncSpanEvent(span, 0, (short)1, 1);
        SpanEvent asyncEvent2 = createAsyncSpanEvent(span, 0, (short)0, 2);
        @SuppressWarnings("unchecked")
        final List<? extends TBase<?,?>> expectedOrder = Arrays.asList(
                span,
                event,
                event1,
                event2,
                asyncEvent1,
                asyncEvent1_1,
                asyncEvent2
        );
        // when
        @SuppressWarnings("unchecked")
        final List<? extends TBase<?,?>> listToBeHandled = Arrays.asList(
                span, event, event1, event2, asyncEvent1, asyncEvent1_1, asyncEvent2
        );
        Collections.shuffle(listToBeHandled);
        for (TBase<?,?> base : listToBeHandled) {
            this.recorder.handleSend(base);
        }
        // then
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.recorder.print(new PrintStream(baos));
        this.logger.debug(baos.toString());
        for (TBase<?,?> expectedBase : expectedOrder) {
            TBase<?,?> actualBase = this.recorder.pop();
            assertSame(expectedBase, actualBase);
        }
        assertNull(this.recorder.pop());
    }
    
    @Test
    public void testMultipleSpanOrdering() {
        // given
        final long startTime1 = System.currentTimeMillis();
        final long spanId1 = 1L;
        final long startTime2 = startTime1 + 10L;
        final long spanId2 = 2L;
        Span span1 = createSpan(startTime1, spanId1);
        SpanEvent event1_0 = createSpanEvent(span1, 1, (short)0);
        SpanEvent event1_1 = createSpanEvent(span1, 2, (short)1);
        SpanEvent asyncEvent1_0 = createAsyncSpanEvent(span1, 1, (short)0, 1);
        SpanEvent asyncEvent1_1 = createAsyncSpanEvent(span1, 2, (short)1, 1);
        Span span2 = createSpan(startTime2, spanId2);
        SpanEvent event2_0 = createSpanEvent(span2, 0, (short)0);
        SpanEvent event2_1 = createSpanEvent(span2, 1, (short)1);
        SpanEvent asyncEvent2_0 = createAsyncSpanEvent(span2, 0, (short)0, 1);
        @SuppressWarnings("unchecked")
        final List<? extends TBase<?,?>> expectedOrder = Arrays.asList(
                span1,
                event1_0,
                event1_1,
                span2,
                event2_0,
                event2_1,
                asyncEvent1_0,
                asyncEvent1_1,
                asyncEvent2_0
        );
        // when
        @SuppressWarnings("unchecked")
        final List<? extends TBase<?,?>> listToBeHandled = Arrays.asList(
                span1, event1_0, event1_1, span2, event2_0, event2_1, asyncEvent1_0, asyncEvent1_1, asyncEvent2_0
        );
        Collections.shuffle(listToBeHandled);
        for (TBase<?,?> base : listToBeHandled) {
            this.recorder.handleSend(base);
        }
        // then
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.recorder.print(new PrintStream(baos));
        this.logger.debug(baos.toString());
        for (TBase<?,?> expectedBase : expectedOrder) {
            TBase<?,?> actualBase = this.recorder.pop();
            assertSame(expectedBase, actualBase);
        }
        assertNull(this.recorder.pop());
    }
    
    private SpanEvent createSpanEvent(Span associatedSpan, int startElapsed, short sequence) {
        return createAsyncSpanEvent(associatedSpan, startElapsed, sequence, UNSET_ASYNC_ID);
    }
    
    private SpanEvent createAsyncSpanEvent(Span associatedSpan, int startElapsed, short sequence, int asyncId) {
        if (startElapsed < 0) {
            throw new IllegalArgumentException("startElapsed cannot be less than 0");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence cannot be less than 0");
        }
        if (asyncId < 0) {
            throw new IllegalArgumentException("asyncId cannot be less than 0");
        }
        SpanEvent event = new SpanEvent(associatedSpan);
        event.setStartElapsed(startElapsed);
        event.setSequence(sequence);
        if (asyncId != UNSET_ASYNC_ID) {
            event.setAsyncId(asyncId);
        }
        return event;
    }
    
    private Span createSpan(long startTime, long spanId) {
        Span span = new Span();
        span.setStartTime(startTime);
        span.setSpanId(spanId);
        return span;
    }

}
