package com.navercorp.pinpoint.profiler.context.id;

import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class LocalTraceRootImpl implements LocalTraceRoot {

    protected final String agentId;
    protected final long localTransactionId;

    protected final long traceStartTime;

    protected final Shared shared = new DefaultShared();


    LocalTraceRootImpl(String agentId, long traceStartTime, long localTransactionId) {
        this.agentId = Objects.requireNonNull(agentId, "agentId");
        this.traceStartTime = traceStartTime;
        this.localTransactionId = localTransactionId;
    }

    public String getAgentId() {
        return agentId;
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
        return "LocalTraceRootImpl{" +
                "agentId='" + agentId + '\'' +
                ", localTransactionId=" + localTransactionId +
                ", traceStartTime=" + traceStartTime +
                ", shared=" + shared +
                '}';
    }
}
