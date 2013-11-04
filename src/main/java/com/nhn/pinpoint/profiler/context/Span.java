package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.common.util.TransactionIdUtils;
import com.nhn.pinpoint.thrift.dto.TIntStringValue;
import com.nhn.pinpoint.thrift.dto.TSpan;

/**
 * Span represent RPC
 *
 * @author netspider
 */
public class Span extends TSpan {
    public Span() {
    }

    public void recordTraceId(final TraceId traceId) {
        if (traceId == null) {
            throw new NullPointerException("traceId must not be null");
        }
        final String agentId = this.getAgentId();
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }

        final String transactionAgentId = traceId.getAgentId();
        if (!agentId.equals(transactionAgentId)) {
            this.setTransactionId(TransactionIdUtils.formatBytes(transactionAgentId, traceId.getAgentStartTime(), traceId.getTransactionSequence()));
        } else {
            this.setTransactionId(TransactionIdUtils.formatBytes(null, traceId.getAgentStartTime(), traceId.getTransactionSequence()));
        }

        this.setSpanId(traceId.getSpanId());
        final long parentSpanId = traceId.getParentSpanId();
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


}
