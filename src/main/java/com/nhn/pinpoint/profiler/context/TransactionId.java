package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public class TransactionId {
    private final String agentId;
    private long agentStartTime;
    private long transactionId;

    public TransactionId(String agentId, long agentStartTime, long transactionId) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionId = transactionId;
    }

    public String getAgentId() {
        return agentId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public long getTransactionId() {
        return transactionId;
    }
}
