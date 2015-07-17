package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;

public class DefaultAsyncTraceId implements AsyncTraceId {

    private final TraceId traceId;
    private final int asyncId;
    private final long startTime;
    private short asyncSequence = 0;
    
    public DefaultAsyncTraceId(final TraceId traceId, final int asyncId, final long startTime) {
        this.traceId = traceId;
        this.asyncId = asyncId;
        this.startTime = startTime;
    }
    
    public int getAsyncId() {
        return asyncId;
    }

    public synchronized short nextAsyncSequence() {
        this.asyncSequence += 1;
        return this.asyncSequence;
    }
    
    @Override
    public TraceId getNextTraceId() {
        return traceId.getNextTraceId();
    }

    @Override
    public long getSpanId() {
        return traceId.getSpanId();
    }

    @Override
    public String getTransactionId() {
        return traceId.getTransactionId();
    }

    @Override
    public String getAgentId() {
        return traceId.getAgentId();
    }

    @Override
    public long getAgentStartTime() {
        return traceId.getAgentStartTime();
    }

    @Override
    public long getTransactionSequence() {
        return traceId.getTransactionSequence();
    }

    @Override
    public long getParentSpanId() {
        return traceId.getParentSpanId();
    }

    @Override
    public short getFlags() {
        return traceId.getFlags();
    }

    @Override
    public boolean isRoot() {
        return traceId.isRoot();
    }

    @Override
    public long getSpanStartTime() {
        return startTime;
    }

    @Override
    public TraceId getParentTraceId() {
        return traceId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{traceId=");
        builder.append(traceId);
        builder.append(", asyncId=");
        builder.append(asyncId);
        builder.append(", startTime=");
        builder.append(startTime);
        builder.append("}");
        return builder.toString();
    }
}