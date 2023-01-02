package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LocalTraceRoot implements TraceRoot {

    private final String agentId;
    private final long localTransactionId;

    private final long traceStartTime;

    private final Shared shared = new DefaultShared();


    public LocalTraceRoot(String agentId, long traceStartTime, long localTransactionId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.traceStartTime = traceStartTime;
        this.localTransactionId = localTransactionId;
    }

    @Override
    public TraceId getTraceId() {
        return null;
    }

    @Override
    public long getLocalTransactionId() {
        return localTransactionId;
    }


    @Override
    public long getTraceStartTime() {
        return traceStartTime;
    }


    @Override
    public Shared getShared() {
        return shared;
    }


    @Override
    public String toString() {
        return "DisableTraceRoot{" +
                ", agentId='" + agentId + '\'' +
                ", traceStartTime=" + traceStartTime +
                '}';
    }
}
