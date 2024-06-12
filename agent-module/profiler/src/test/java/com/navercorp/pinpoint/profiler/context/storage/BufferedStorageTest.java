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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.sender.CountingDataSender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BufferedStorageTest {

    private final AgentId agentId = AgentId.of("agentId");
    private final long agentStartTime = System.currentTimeMillis();

    private final CountingDataSender countingDataSender = new CountingDataSender();
    private TraceRoot internalTraceId;

    @BeforeEach
    public void before() {
        countingDataSender.stop();
        internalTraceId = newInternalTraceId();
    }

    private TraceRoot newInternalTraceId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime, 100);
        return TraceRoot.remote(traceId, agentId, agentStartTime, 100);
    }

    @Test
    public void testStore_Noflush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assertions.assertEquals(0, countingDataSender.getTotalCount());
    }

    @Test
    public void testStore_flush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(1);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assertions.assertEquals(2, countingDataSender.getSenderCounter());
        Assertions.assertEquals(2, countingDataSender.getTotalCount());

        Assertions.assertEquals(2, countingDataSender.getSpanChunkCounter());
        Assertions.assertEquals(0, countingDataSender.getSpanCounter());
    }


    @Test
    public void testStore_spanFlush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        bufferedStorage.store(span);
        bufferedStorage.store(span);
        bufferedStorage.store(span);

        Assertions.assertEquals(3, countingDataSender.getSenderCounter());
        Assertions.assertEquals(3, countingDataSender.getTotalCount());

        Assertions.assertEquals(3, countingDataSender.getSpanCounter());
        Assertions.assertEquals(0, countingDataSender.getSpanChunkCounter());
    }

    @Test
    public void testStore_spanLastFlush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(span);

        Assertions.assertEquals(1, countingDataSender.getSenderCounter());
        Assertions.assertEquals(1, countingDataSender.getTotalCount());

        Assertions.assertEquals(1, countingDataSender.getSpanCounter());
        Assertions.assertEquals(0, countingDataSender.getSpanChunkCounter());
    }

    @Test
    public void testStore_manual_flush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);
        bufferedStorage.flush();

        Assertions.assertEquals(1, countingDataSender.getSenderCounter());
        Assertions.assertEquals(1, countingDataSender.getTotalCount());

        Assertions.assertEquals(0, countingDataSender.getSpanCounter());
        Assertions.assertEquals(1, countingDataSender.getSpanChunkCounter());
    }

    private BufferedStorage newBufferedStorage(int bufferSize) {
        SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(internalTraceId);
        return new BufferedStorage(spanChunkFactory, countingDataSender, bufferSize);
    }
}