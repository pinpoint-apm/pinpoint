package com.nhn.pinpoint.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpanChunk;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;
import org.apache.thrift.TBase;

import com.nhn.pinpoint.profiler.DefaultAgent;

/**
 *
 */
public class SpanChunk extends TSpanChunk implements Thriftable {

    public SpanChunk(List<SpanEvent> spanEventList) {
        if (spanEventList == null) {
            throw new NullPointerException("spanEventList must not be null");
        }
        setSpanEventList((List) spanEventList);
    }

    @Override
    public TBase toThrift() {
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        final List<TSpanEvent> spanEventList = getSpanEventList();
        TSpanEvent first = spanEventList.get(0);
        if (first == null) {
            throw new IllegalStateException("fist spanEvent not found");
        }
        Span parentSpan = ((SpanEvent)first).getSpan();

        final AgentInformation agentInformation = DefaultAgent.getInstance().getAgentInformation();
        this.setAgentId(agentInformation.getAgentId());
        this.setApplicationName(agentInformation.getApplicationName());
        this.setAgentStartTime(agentInformation.getStartTime());

        this.setServiceType(parentSpan.getServiceType());

        this.setTraceAgentId(parentSpan.getTraceAgentId());
        this.setTraceAgentStartTime(parentSpan.getTraceAgentStartTime());
        this.setTraceTransactionSequence(parentSpan.getTraceTransactionSequence());
        this.setSpanId(parentSpan.getSpanId());

        this.setEndPoint(parentSpan.getEndPoint());
        
        List<TSpanEvent> tSpanEvent = createTSpanEvent(spanEventList);

        this.setSpanEventList(tSpanEvent);

        return this;
    }

    private List<TSpanEvent> createTSpanEvent(List<TSpanEvent> spanEventList) {
        for (TSpanEvent tSpanEvent : spanEventList) {
            if (tSpanEvent instanceof SpanEvent) {
                ((SpanEvent)tSpanEvent).toThrift(true);
            }
        }
        // type check 무시.
        return spanEventList;
    }


}
