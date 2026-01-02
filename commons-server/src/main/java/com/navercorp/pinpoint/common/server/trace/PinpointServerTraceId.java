package com.navercorp.pinpoint.common.server.trace;

import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.util.TransactionIdParser;

import java.util.Objects;

public class PinpointServerTraceId implements ServerTraceId {

    private final String agentId;
    private final long agentStartTime;
    private final long transactionSequence;

    public PinpointServerTraceId(String agentId, long agentStartTime, long transactionSequence) {
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
    }

    @Override
    public byte[] getId() {
        return TransactionIdParser.getVarTransactionId(agentId, agentStartTime, transactionSequence);
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionSequence() {
        return transactionSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        PinpointServerTraceId that = (PinpointServerTraceId) o;
        return agentStartTime == that.agentStartTime && transactionSequence == that.transactionSequence && Objects.equals(agentId, that.agentId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(agentId);
        result = 31 * result + Long.hashCode(agentStartTime);
        result = 31 * result + Long.hashCode(transactionSequence);
        return result;
    }

    @Override
    public String toString() {
        return TransactionIdUtils.formatString(agentId, agentStartTime, transactionSequence);
    }
}
