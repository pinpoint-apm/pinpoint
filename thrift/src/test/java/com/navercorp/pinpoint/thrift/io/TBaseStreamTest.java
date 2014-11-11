package com.nhn.pinpoint.thrift.io;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.junit.Test;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

public class TBaseStreamTest {
    private static final String AGENT_ID = "agentId";
    private static final String APPLICATION_NAME = "applicationName";
    private static final byte[] TRANSACTION_ID = "transactionId".getBytes();
    private static final long START_TIME = System.currentTimeMillis();
    private static final short SERVICE_TYPE = Short.valueOf("1");

    private static final TProtocolFactory DEFAULT_PROTOCOL_FACTORY = new TCompactProtocol.Factory();
    
    @Test
    public void clear() throws Exception {
        TBaseStream stream = new TBaseStream(DEFAULT_PROTOCOL_FACTORY);
        TSpanEvent spanEvent = newSpanEvent();
        stream.write(spanEvent);
        assertTrue(stream.size() > 0);

        stream.clear();
        assertTrue(stream.size() == 0);
    }

    @Test
    public void write() throws Exception {
        TBaseStream stream = new TBaseStream(DEFAULT_PROTOCOL_FACTORY);

        // single span event
        TSpanEvent spanEvent = newSpanEvent();
        stream.write(spanEvent);
        int size = stream.size();

        // append
        stream.write(spanEvent);
        assertEquals(size * 2, stream.size());
    }

    @Test
    public void splite() throws Exception {
        TBaseStream stream = new TBaseStream(DEFAULT_PROTOCOL_FACTORY);

        TSpanEvent spanEvent = newSpanEvent();
        stream.write(spanEvent);
        int size = stream.size();

        // append 3
        stream.write(spanEvent);
        stream.write(spanEvent);
        stream.write(spanEvent);

        // split 1
        List<ByteArrayOutput> nodes = stream.split(size);
        assertEquals(1, nodes.size());

        // split 2
        nodes = stream.split(size * 2);
        assertEquals(2, nodes.size());

        // split 1
        nodes = stream.split(1);
        assertEquals(1, nodes.size());

        nodes = stream.split(1);
        assertEquals(0, nodes.size());
    }

    @Test
    public void chunk() throws Exception {
        TBaseStream stream = new TBaseStream(DEFAULT_PROTOCOL_FACTORY);

        final String str1k = RandomStringUtils.randomAlphabetic(1024);

        // chunk
        TSpanChunk spanChunk = newSpanChunk();
        List<TSpanEvent> spanEventList = new ArrayList<TSpanEvent>();
        spanChunk.setSpanEventList(spanEventList);
        spanChunk.setSpanEventListIsSet(true);

        TSpanEvent event1k = new TSpanEvent();
        event1k.setDestinationId(str1k);
        event1k.setDestinationIdIsSet(true);

        // add 2 event
        spanEventList.add(event1k);
        spanEventList.add(event1k);

        // write event
        for (TSpanEvent e : spanChunk.getSpanEventList()) {
            stream.write(e);
        }
        System.out.println("event " + stream);

        // split 1k
        TBaseStream chunkStream = new TBaseStream(DEFAULT_PROTOCOL_FACTORY);

        List<ByteArrayOutput> nodes = stream.split(1024);
        System.out.println("nodes " + nodes);
//        chunkStream.write(spanChunk, "spanEventList", nodes);
//        while (!stream.isEmpty()) {
//            nodes = stream.split(1024);
//            System.out.println("nodes " + nodes);
//            chunkStream.write(spanChunk, "spanEventList", nodes);
//        }
//
//        System.out.println("chunk " + chunkStream);
    }

    public TSpanChunk newSpanChunk() {
        final TSpanChunk spanChunk = new TSpanChunk();
        spanChunk.setAgentId(AGENT_ID);
        spanChunk.setAgentIdIsSet(true);

        spanChunk.setApplicationName(APPLICATION_NAME);
        spanChunk.setApplicationNameIsSet(true);

        spanChunk.setAgentStartTime(START_TIME);
        spanChunk.setAgentStartTimeIsSet(true);

        spanChunk.setServiceType(SERVICE_TYPE);
        spanChunk.setServiceTypeIsSet(true);

        spanChunk.setTransactionId(TRANSACTION_ID);
        spanChunk.setTransactionIdIsSet(true);

        spanChunk.setSpanId(1);
        spanChunk.setSpanIdIsSet(true);

        List<TSpanEvent> list = new ArrayList<TSpanEvent>();
        list.add(newSpanEvent());
        list.add(newSpanEvent());
        list.add(newSpanEvent());
        spanChunk.setSpanEventList(list);
        spanChunk.setSpanEventListIsSet(true);

        return spanChunk;
    }

    private TSpan newSpan() {
        final TSpan span = new TSpan();
        span.setAgentId(AGENT_ID);
        span.setAgentIdIsSet(true);

        span.setApplicationName(APPLICATION_NAME);
        span.setApplicationNameIsSet(true);

        span.setTransactionId(TRANSACTION_ID);
        span.setTransactionIdIsSet(true);

        span.setSpanId(1);
        span.setSpanIdIsSet(true);

        span.setStartTime(START_TIME);
        span.setStartTimeIsSet(true);

        span.setServiceType(SERVICE_TYPE);
        span.setServiceTypeIsSet(true);

        return span;
    }

    private TSpanEvent newSpanEvent() {
        TSpanEvent spanEvent = new TSpanEvent();

        spanEvent.setApiId(1);
        spanEvent.setApiIdIsSet(true);

        spanEvent.setDepth(1);
        spanEvent.setDepthIsSet(true);

        spanEvent.setEndElapsed(1);
        spanEvent.setEndElapsedIsSet(true);

        spanEvent.setDestinationId("db");
        spanEvent.setDestinationIdIsSet(true);

        spanEvent.setEndPoint("10.10.10.10");
        spanEvent.setEndPointIsSet(true);

        return spanEvent;
    }
}