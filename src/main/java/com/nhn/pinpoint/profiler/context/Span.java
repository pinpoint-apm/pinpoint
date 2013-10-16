package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.profiler.AgentInformation;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;
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
        final int after = (int)(System.currentTimeMillis() - this.getStartTime());
        // long으로 바꿀것.
        if (after != 0) {
            this.setElapsed(after);
        }
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

    public void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        final TIntStringValue exceptionInfo = new TIntStringValue(exceptionClassId);
        if (exceptionMessage != null && !exceptionMessage.isEmpty()) {
            exceptionInfo.setStringValue(exceptionMessage);
        }
        super.setExceptionInfo(exceptionInfo);
    }

    public boolean isSetErrCode() {
        return isSetErr();
    }

    public int getErrCode() {
		return getErr();
	}

	public void setErrCode(int exception) {
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
