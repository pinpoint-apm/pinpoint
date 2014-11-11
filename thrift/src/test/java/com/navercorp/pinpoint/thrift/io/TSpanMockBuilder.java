package com.nhn.pinpoint.thrift.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

public class TSpanMockBuilder {
    private static final String AGENT_ID = "agentId";
    private static final String APPLICATION_NAME = "applicationName";
    private static final byte[] TRANSACTION_ID = "transactionId".getBytes();
    private static final short SERVICE_TYPE = Short.valueOf("1");

    private String agentId = AGENT_ID;
    private String applicationName = APPLICATION_NAME;
    private byte[] transactionId = TRANSACTION_ID;
    private long startTime = System.currentTimeMillis();
    private short serviceType = SERVICE_TYPE;

    public TSpanMockBuilder() {
    }

    public TSpanMockBuilder setAgentId(final String agentId) {
        this.agentId = agentId;
        return this;
    }

    public TSpanMockBuilder setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public TSpanMockBuilder setTransactionId(final String transactionId) {
        this.transactionId = transactionId.getBytes();
        return this;
    }

    public TSpanMockBuilder setServiceType(final short serviceType) {
        this.serviceType = serviceType;
        return this;
    }

    public TSpan build(int spanEventCount, int spanEventSize) {

        final TSpan span = new TSpan();
        span.setAgentId(agentId);
        span.setAgentIdIsSet(true);

        span.setApplicationName(applicationName);
        span.setApplicationNameIsSet(true);

        span.setTransactionId(transactionId);
        span.setTransactionIdIsSet(true);

        span.setSpanId(1);
        span.setSpanIdIsSet(true);

        span.setStartTime(startTime);
        span.setStartTimeIsSet(true);

        span.setServiceType(serviceType);
        span.setServiceTypeIsSet(true);

        for (int i = 0; i < spanEventCount; i++) {
            span.addToSpanEventList(buildEvent(spanEventSize));
            span.setSpanEventListIsSet(true);
        }

        return span;
    }

    public TSpanChunk buildChunk(int spanEventCount, int spanEventSize) {
        final TSpanChunk spanChunk = new TSpanChunk();
        spanChunk.setAgentId(agentId);
        spanChunk.setAgentIdIsSet(true);

        spanChunk.setApplicationName(applicationName);
        spanChunk.setApplicationNameIsSet(true);

        spanChunk.setAgentStartTime(startTime);
        spanChunk.setAgentStartTimeIsSet(true);

        spanChunk.setServiceType(serviceType);
        spanChunk.setServiceTypeIsSet(true);

        spanChunk.setTransactionId(transactionId);
        spanChunk.setTransactionIdIsSet(true);

        spanChunk.setSpanId(1);
        spanChunk.setSpanIdIsSet(true);

        for (int i = 0; i < spanEventCount; i++) {
            spanChunk.addToSpanEventList(buildEvent(spanEventSize));
            spanChunk.setSpanEventListIsSet(true);
        }

        return spanChunk;
    }

    public TSpanEvent buildEvent(int size) {
        TSpanEvent spanEvent = new TSpanEvent();

        spanEvent.setApiId(1);
        spanEvent.setApiIdIsSet(true);

        spanEvent.setDepth(1);
        spanEvent.setDepthIsSet(true);

        byte[] buffer = new byte[size];
        Arrays.fill(buffer, (byte) 1);

        spanEvent.setDestinationId(new String(buffer));
        spanEvent.setDestinationIdIsSet(true);

        return spanEvent;
    }
}