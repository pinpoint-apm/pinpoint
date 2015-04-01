package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.TraceId;

public class AsyncTraceId implements TraceId {

    private final TraceId traceId;
    private final int asyncId;
    private final long startTime;
    
    public AsyncTraceId(final TraceId traceId, final int asyncId, final int startTime) {
        this.traceId = traceId;
        this.asyncId = asyncId;
        this.startTime = startTime;
    }
    
    public int getAsyncId() {
        return asyncId;
    }

    public long getStartTime() {
        return startTime;
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
}