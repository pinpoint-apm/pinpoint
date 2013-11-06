package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.List;

/**
 * @author emeroad
 */
public class SpanChunkFactory {

    private final AgentInformation agentInformation;

    public SpanChunkFactory(AgentInformation agentInformation) {
        if (agentInformation == null) {
            throw new NullPointerException("agentInformation must not be null");
        }
        this.agentInformation = agentInformation;
    }

    public SpanChunk create(final List<SpanEvent> flushData) {
        if (flushData == null) {
            throw new NullPointerException("flushData must not be null");
        }
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        if (flushData.size() > 0) {
            throw new IllegalArgumentException("flushData.size() > 0 " + flushData.size());
        }


        final SpanEvent first = flushData.get(0);
        if (first == null) {
            throw new IllegalStateException("first spanEvent not found");
        }
        final Span parentSpan = first.getSpan();
        final String agentId = this.agentInformation.getAgentId();

        final SpanChunk spanChunk = new SpanChunk(flushData);
        spanChunk.setAgentId(agentId);
        spanChunk.setApplicationName(this.agentInformation.getApplicationName());
        spanChunk.setAgentStartTime(this.agentInformation.getStartTime());

        spanChunk.setServiceType(parentSpan.getServiceType());


        final byte[] transactionId = parentSpan.getTransactionId();
        spanChunk.setTransactionId(transactionId);


        spanChunk.setSpanId(parentSpan.getSpanId());

        spanChunk.setEndPoint(parentSpan.getEndPoint());
        return spanChunk;
    }
}
