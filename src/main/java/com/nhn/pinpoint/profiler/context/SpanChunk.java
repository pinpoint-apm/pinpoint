package com.nhn.pinpoint.profiler.context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.thrift.TBase;

import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.thrift.dto.Annotation;

/**
 *
 */
public class SpanChunk implements Thriftable {

    private List<SpanEvent> spanEventList = new ArrayList<SpanEvent>();

    public SpanChunk(List<SpanEvent> spanEventList) {
        this.spanEventList = spanEventList;
    }

    @Override
    public TBase toThrift() {
        com.nhn.pinpoint.thrift.dto.SpanChunk tSpanChunk = new com.nhn.pinpoint.thrift.dto.SpanChunk();
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        SpanEvent first = spanEventList.get(0);
        Span parentSpan = first.getParentSpan();

        DefaultAgent agent = DefaultAgent.getInstance();
        tSpanChunk.setAgentId(agent.getAgentId());
        tSpanChunk.setApplicationName(agent.getApplicationName());
        tSpanChunk.setAgentStartTime(agent.getStartTime());

        tSpanChunk.setServiceType(parentSpan.getServiceType().getCode());

        TraceID traceID = parentSpan.getTraceID();
        tSpanChunk.setTraceAgentId(traceID.getAgentId());
        tSpanChunk.setAgentStartTime(traceID.getAgentStartTime());
        tSpanChunk.setTraceTransactionId(traceID.getTransactionId());
        tSpanChunk.setSpanId(traceID.getSpanId());
        
        tSpanChunk.setEndPoint(parentSpan.getEndPoint());
        
        List<com.nhn.pinpoint.thrift.dto.SpanEvent> tSpanEvent = createSpanEvent(spanEventList);

        tSpanChunk.setSpanEventList(tSpanEvent);

        return tSpanChunk;
    }

    private List<com.nhn.pinpoint.thrift.dto.SpanEvent> createSpanEvent(List<SpanEvent> spanEventList) {
        List<com.nhn.pinpoint.thrift.dto.SpanEvent> result = new ArrayList<com.nhn.pinpoint.thrift.dto.SpanEvent>(spanEventList.size());
        for (SpanEvent spanEvent : spanEventList) {
            com.nhn.pinpoint.thrift.dto.SpanEvent tSpanEvent = new com.nhn.pinpoint.thrift.dto.SpanEvent();

//            tSpanEvent.setAgentId(Agent.getInstance().getAgentId());
//            tSpanEvent.setApplicationName(Agent.getInstance().getApplicationName());
//            tSpanEvent.setAgentIdentifier(Agent.getInstance().getIdentifier());

            long parentSpanStartTime = spanEvent.getParentSpan().getStartTime();
            tSpanEvent.setStartElapsed((int) (spanEvent.getStartTime() - parentSpanStartTime));
            tSpanEvent.setEndElapsed((int) (spanEvent.getEndTime() - spanEvent.getStartTime()));

            tSpanEvent.setSequence(spanEvent.getSequence());

            tSpanEvent.setRpc(spanEvent.getRpc());
            tSpanEvent.setServiceType(spanEvent.getServiceType().getCode());
            tSpanEvent.setDestinationId(spanEvent.getDestionationId());

            tSpanEvent.setEndPoint(spanEvent.getEndPoint());

            // 여기서 데이터 인코딩을 하자.
            List<Annotation> annotationList = new ArrayList<Annotation>(spanEvent.getAnnotationSize());
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
