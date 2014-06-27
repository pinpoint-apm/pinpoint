package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.context.storage.BufferedStorage;
import com.nhn.pinpoint.profiler.sender.CountingDataSender;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

public class BufferedStorageTest {

    private AgentInformation agentInformation = new AgentInformation("agentId", "applicationName", 0, 1, "hostName", ServiceType.TOMCAT.getCode(), Version.VERSION);
    private SpanChunkFactory spanChunkFactory = new SpanChunkFactory(agentInformation);
    private CountingDataSender countingDataSender = new CountingDataSender();

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