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
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.sender.CountingDataSender;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BufferedStorageTest {

    private final String agentId = "agentId";
    private final long agentStartTime = System.currentTimeMillis();

    private final CountingDataSender countingDataSender = new CountingDataSender();
    private TraceRoot internalTraceId;

    @Before
    public void before() {
        countingDataSender.stop();
        internalTraceId = newInternalTraceId();
    }

    private TraceRoot newInternalTraceId() {
        TraceId traceId = new DefaultTraceId(agentId, agentStartTime, 100);
        return new DefaultTraceRoot(traceId, agentId, agentStartTime, 100);
    }

    @Test
    public void testStore_Noflush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assert.assertEquals(0, countingDataSender.getTotalCount());
    }

    @Test
    public void testStore_flush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(1);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assert.assertEquals(2, countingDataSender.getSenderCounter());
        Assert.assertEquals(2, countingDataSender.getTotalCount());

        Assert.assertEquals(2, countingDataSender.getSpanChunkCounter());
        Assert.assertEquals(0, countingDataSender.getSpanCounter());
    }


    @Test
    public void testStore_spanFlush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        bufferedStorage.store(span);
        bufferedStorage.store(span);
        bufferedStorage.store(span);

        Assert.assertEquals(3, countingDataSender.getSenderCounter());
        Assert.assertEquals(3, countingDataSender.getTotalCount());

        Assert.assertEquals(3, countingDataSender.getSpanCounter());
        Assert.assertEquals(0, countingDataSender.getSpanChunkCounter());
    }

    @Test
    public void testStore_spanLastFlush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(span);

        Assert.assertEquals(1, countingDataSender.getSenderCounter());
        Assert.assertEquals(1, countingDataSender.getTotalCount());

        Assert.assertEquals(1, countingDataSender.getSpanCounter());
        Assert.assertEquals(0, countingDataSender.getSpanChunkCounter());
    }

    @Test
    public void testStore_manual_flush() throws Exception {
        BufferedStorage bufferedStorage = newBufferedStorage(10);

        Span span = new Span(internalTraceId);
        SpanEvent spanEvent = new SpanEvent();
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);
        bufferedStorage.flush();

        Assert.assertEquals(1, countingDataSender.getSenderCounter());
        Assert.assertEquals(1, countingDataSender.getTotalCount());

        Assert.assertEquals(0, countingDataSender.getSpanCounter());
        Assert.assertEquals(1, countingDataSender.getSpanChunkCounter());
    }

    private BufferedStorage newBufferedStorage(int bufferSize) {
        SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory(internalTraceId);
        return new BufferedStorage(spanChunkFactory, countingDataSender, bufferSize);
    }
}