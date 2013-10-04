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
public class SpanChunk implements Thriftable {

    private final List<SpanEvent> spanEventList;

    public SpanChunk(List<SpanEvent> spanEventList) {
        if (spanEventList == null) {
            throw new NullPointerException("spanEventList must not be null");
        }
        this.spanEventList = spanEventList;
    }

    @Override
    public TBase toThrift() {
        TSpanChunk tSpanChunk = new TSpanChunk();
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        SpanEvent first = spanEventList.get(0);
        Span parentSpan = first.getParentSpan();

        final AgentInformation agentInformation = DefaultAgent.getInstance().getAgentInformation();
        tSpanChunk.setAgentId(agentInformation.getAgentId());
        tSpanChunk.setApplicationName(agentInformation.getApplicationName());
        tSpanChunk.setAgentStartTime(agentInformation.getStartTime());

        tSpanChunk.setServiceType(parentSpan.getServiceType());

        tSpanChunk.setTraceAgentId(parentSpan.getTraceAgentId());
        tSpanChunk.setTraceAgentStartTime(parentSpan.getTraceAgentStartTime());
        tSpanChunk.setTraceTransactionSequence(parentSpan.getTraceTransactionSequence());
        tSpanChunk.setSpanId(parentSpan.getSpanId());
        
        tSpanChunk.setEndPoint(parentSpan.getEndPoint());
        
        List<TSpanEvent> tSpanEvent = createSpanEvent(spanEventList);

        tSpanChunk.setSpanEventList(tSpanEvent);

        return tSpanChunk;
    }

    private List<TSpanEvent> createSpanEvent(List<SpanEvent> spanEventList) {
        List<TSpanEvent> result = new ArrayList<TSpanEvent>(spanEventList.size());
        for (SpanEvent spanEvent : spanEventList) {
            TSpanEvent tSpanEvent = new TSpanEvent();

//            tSpanEvent.setAgentId(Agent.getInstance().getAgentId());
//            tSpanEvent.setApplicationName(Agent.getInstance().getApplicationName());
//            tSpanEvent.setAgentIdentifier(Agent.getInstance().getPid());

            long parentSpanStartTime = spanEvent.getParentSpan().getStartTime();
            tSpanEvent.setStartElapsed((int) (spanEvent.getStartTime() - parentSpanStartTime));
            tSpanEvent.setEndElapsed((int) (spanEvent.getEndTime() - spanEvent.getStartTime()));

            tSpanEvent.setSequence(spanEvent.getSequence());

            tSpanEvent.setRpc(spanEvent.getRpc());
            tSpanEvent.setServiceType(spanEvent.getServiceType().getCode());
            tSpanEvent.setDestinationId(spanEvent.getDestionationId());

            tSpanEvent.setEndPoint(spanEvent.getEndPoint());

            // 여기서 데이터 인코딩을 하자.
            List<TAnnotation> annotationList = new ArrayList<TAnnotation>(spanEvent.getAnnotationSize());
            for (TraceAnnotation traceAnnotation : spanEvent.getTraceAnnotationList()) {
                annotationList.add(traceAnnotation.toThrift());
            }
            
			if (spanEvent.getDepth() != -1) {
				tSpanEvent.setDepth(spanEvent.getDepth());
			}

			if (spanEvent.getNextSpanId() != -1) {
				tSpanEvent.setNextSpanId(spanEvent.getNextSpanId());
			}
            
            tSpanEvent.setAnnotations(annotationList);
            result.add(tSpanEvent);
        }
        return result;
    }


}
