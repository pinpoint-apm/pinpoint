package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.thrift.dto.TSpan;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span extends TSpan implements Thriftable {
    private final TraceId traceId;

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

    public void markAfterTime() {
        if (!isSetStartTime()) {
            throw new PinpointTraceException("startTime is not set");
        }
        final long startTime = this.getStartTime();
        // long으로 바꿀것.
        this.setElapsed((int)(System.currentTimeMillis() - startTime));
    }

    public long getAfterTime() {
        if (!isSetStartTime()) {
            throw new PinpointTraceException("startTime is not set");
        }
        return this.getStartTime() + this.getElapsed();
    }


    public void addAnnotation(Annotation annotation) {
        this.addToAnnotations(annotation);
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
        final String agentId = agentInformation.getAgentId();
        if (agentId.equals(this.getTraceAgentId())) {
            this.unsetTraceAgentId();
        }
        this.setAgentId(agentInformation.getAgentId());
        this.setApplicationName(agentInformation.getApplicationName());
        this.setAgentStartTime(agentInformation.getStartTime());

        return this;
    }


}
