package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span extends TSpan implements Thriftable {
    private final TraceId traceId;

    private final List<TraceAnnotation> traceAnnotationList = new ArrayList<TraceAnnotation>(5);

    private List<SpanEvent> spanEventList;
    
    public Span(TraceId traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        this.traceId = traceId;
    }

    private void recordTraceId(TraceId traceId) {
        this.setTraceAgentId(traceId.getAgentId());
        this.setTraceAgentStartTime(traceId.getAgentStartTime());
        this.setTraceTransactionSequence(traceId.getTransactionSequence());

        this.setSpanId(traceId.getSpanId());
        final int parentSpanId = traceId.getParentSpanId();
        if (traceId.getParentSpanId() != SpanId.NULL) {
            this.setParentSpanId(parentSpanId);
        }
        this.setFlag(traceId.getFlags());
    }


    public boolean addAnnotation(TraceAnnotation traceAnnotation) {
        return traceAnnotationList.add(traceAnnotation);
    }

    public int getAnnotationSize() {
        return traceAnnotationList.size();
    }

    public List<SpanEvent> getPSpanEventList() {
        return spanEventList;
    }

    public void setPSpanEventList(List<SpanEvent> spanEventList) {
        this.spanEventList = spanEventList;
    }
    
    public int getException() {
		return getErr();
	}

	public void setException(int exception) {
        super.setErr(exception);
	}

    public TraceId getTraceId() {
        return traceId;
    }

    public TSpan toThrift() {

        recordTraceId(traceId);
        final AgentInformation agentInformation = DefaultAgent.getInstance().getAgentInformation();
        this.setAgentId(agentInformation.getAgentId());
        this.setApplicationName(agentInformation.getApplicationName());
        this.setAgentStartTime(agentInformation.getStartTime());


        // 여기서 데이터 인코딩을 하자.
        List<TAnnotation> annotationList = new ArrayList<TAnnotation>(traceAnnotationList.size());
        for (TraceAnnotation traceAnnotation : traceAnnotationList) {
            annotationList.add(traceAnnotation.toThrift());
        }
        this.setAnnotations(annotationList);

        List<SpanEvent> spanEventList = this.getPSpanEventList();
        if (spanEventList != null && spanEventList.size() != 0) {
            List<TSpanEvent> tSpanEventList = new ArrayList<TSpanEvent>(spanEventList.size());
            for (SpanEvent spanEvent : spanEventList) {
                TSpanEvent tSpanEvent = spanEvent.toThrift(true);
                tSpanEventList.add(tSpanEvent);
            }
            this.setSpanEventList(tSpanEventList);
        }

        return this;
    }
}
