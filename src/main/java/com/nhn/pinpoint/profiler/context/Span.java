package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.thrift.dto.TAnnotation;
import com.nhn.pinpoint.thrift.dto.TSpan;
import com.nhn.pinpoint.thrift.dto.TSpanEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span extends TSpan implements Thriftable {
    private final TraceId traceId;

    private List<TraceAnnotation> traceAnnotationList = new ArrayList<TraceAnnotation>(4);

    public Span(TraceId traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        this.traceId = traceId;
        recordTraceId(traceId);
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

    public void markBeforeTime() {
        this.setStartTime(System.currentTimeMillis());
    }

    public void markEndTime() {
        if (!isSetStartTime()) {
            throw new RuntimeException("startTime is not set");
        }
        final long startTime = this.getStartTime();
        // long으로 바꿀것.
        this.setElapsed((int)(System.currentTimeMillis() - startTime));
    }

    public long getEndTime() {
        if (!isSetStartTime()) {
            throw new RuntimeException("startTime is not set");
        }
        return this.getStartTime() + this.getElapsed();
    }


    public boolean addAnnotation(TraceAnnotation traceAnnotation) {
        return traceAnnotationList.add(traceAnnotation);
    }

    public int getAnnotationSize() {
        return traceAnnotationList.size();
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
        this.traceAnnotationList = null;

        final List<TSpanEvent> spanEventList = this.getSpanEventList();
        if (spanEventList != null) {
            for (TSpanEvent spanEvent : spanEventList) {
                if (spanEvent instanceof SpanEvent) {
                    ((SpanEvent)spanEvent).toThrift(true);
                }
            }
        }

        return this;
    }


}
