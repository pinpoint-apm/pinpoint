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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.DefaultSpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.sender.CountingDataSender;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BufferedStorageTest {


    private final SpanChunkFactory spanChunkFactory = new DefaultSpanChunkFactory("applicationName", "agentId", 0, ServiceType.STAND_ALONE);
    private final CountingDataSender countingDataSender = new CountingDataSender();

    @Before
    public void before() {
        countingDataSender.stop();
    }

    @Test
    public void testStore_Noflush() throws Exception {
        BufferedStorage bufferedStorage = new BufferedStorage(countingDataSender, spanChunkFactory, 10);

        Span span = new Span();
        SpanEvent spanEvent = new SpanEvent(span);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assert.assertEquals(0, countingDataSender.getTotalCount());
    }

    @Test
    public void testStore_flush() throws Exception {
        BufferedStorage bufferedStorage = new BufferedStorage(countingDataSender, spanChunkFactory, 1);

        Span span = new Span();
        SpanEvent spanEvent = new SpanEvent(span);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);

        Assert.assertEquals(0, countingDataSender.getSenderCounter(), 2);
        Assert.assertEquals(0, countingDataSender.getTotalCount(), 2);

        Assert.assertEquals(0, countingDataSender.getSpanChunkCounter(), 2);
        Assert.assertEquals(0, countingDataSender.getSpanCounter(), 0);
    }


    @Test
    public void testStore_spanFlush() throws Exception {
        BufferedStorage bufferedStorage = new BufferedStorage(countingDataSender, spanChunkFactory, 10);

        Span span = new Span();
        bufferedStorage.store(span);
        bufferedStorage.store(span);
        bufferedStorage.store(span);

        Assert.assertEquals(0, countingDataSender.getSenderCounter(), 3);
        Assert.assertEquals(0, countingDataSender.getTotalCount(), 3);

        Assert.assertEquals(0, countingDataSender.getSpanCounter(), 3);
        Assert.assertEquals(0, countingDataSender.getSpanChunkCounter(), 0);
    }

    @Test
    public void testStore_spanLastFlush() throws Exception {
        BufferedStorage bufferedStorage = new BufferedStorage(countingDataSender, spanChunkFactory, 10);

        Span span = new Span();
        SpanEvent spanEvent = new SpanEvent(span);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(spanEvent);
        bufferedStorage.store(span);

        Assert.assertEquals(0, countingDataSender.getSenderCounter(), 1);
        Assert.assertEquals(0, countingDataSender.getTotalCount(), 1);

        Assert.assertEquals(0, countingDataSender.getSpanCounter(), 1);
        Assert.assertEquals(0, countingDataSender.getSpanChunkCounter(), 0);
    }
}