package com.profiler.context;

import com.profiler.Agent;
import org.apache.thrift.TBase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        com.profiler.common.dto.thrift.SpanChunk tSpanChunk = new com.profiler.common.dto.thrift.SpanChunk();
        // TODO 반드시 1개 이상이라는 조건을 충족해야 된다.
        SpanEvent first = spanEventList.get(0);
        Span parentSpan = first.getParentSpan();
        tSpanChunk.setAgentId(Agent.getInstance().getAgentId());
        tSpanChunk.setAgentIdentifier(Agent.getInstance().getIdentifier());

        UUID id = parentSpan.getTraceID().getId();
        tSpanChunk.setMostTraceId(id.getMostSignificantBits());
        tSpanChunk.setLeastTraceId(id.getLeastSignificantBits());
        tSpanChunk.setSpanId(parentSpan.getTraceID().getSpanId());

        List<com.profiler.common.dto.thrift.SpanEvent> tSpanEvent = createSpanEvent(spanEventList);

        tSpanChunk.setSpanEventList(tSpanEvent);

        return tSpanChunk;
    }

    private List<com.profiler.common.dto.thrift.SpanEvent> createSpanEvent(List<SpanEvent> spanEventList) {
        List<com.profiler.common.dto.thrift.SpanEvent> result = new ArrayList<com.profiler.common.dto.thrift.SpanEvent>(spanEventList.size());
        for (SpanEvent spanEvent : spanEventList) {
            com.profiler.common.dto.thrift.SpanEvent tSpanEvent = new com.profiler.common.dto.thrift.SpanEvent();

            tSpanEvent.setAgentId(Agent.getInstance().getAgentId());
            tSpanEvent.setAgentIdentifier(Agent.getInstance().getIdentifier());

            long parentSpanStartTime = spanEvent.getParentSpan().getStartTime();
            tSpanEvent.setStartElapsed((int) (spanEvent.getStartTime() - parentSpanStartTime));
            tSpanEvent.setEndElapsed((int) (spanEvent.getEndTime() - spanEvent.getStartTime()));

            tSpanEvent.setSequence(spanEvent.getSequence());

            tSpanEvent.setRpc(spanEvent.getRpc());
//            tSpanEvent.setServiceName(spanEvent.getServiceName());
            tSpanEvent.setServiceType(spanEvent.getServiceType().getCode());
            tSpanEvent.setDestinationId(spanEvent.getDestionationId());

            tSpanEvent.setEndPoint(spanEvent.getEndPoint());
            tSpanEvent.setDestinationId(spanEvent.getDestionationId());

            // 여기서 데이터 인코딩을 하자.
            List<com.profiler.common.dto.thrift.Annotation> annotationList = new ArrayList<com.profiler.common.dto.thrift.Annotation>(spanEvent.getAnnotationSize());
            for (Annotation a : spanEvent.getAnnotations()) {
                annotationList.add(a.toThrift());
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
