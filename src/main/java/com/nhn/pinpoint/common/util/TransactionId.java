package com.nhn.pinpoint.common.util;

/**
 * @author emeroad
 */
public class TransactionId {

    protected String agentId;
    protected long agentStartTime;
    protected long transactionSequence;

    public TransactionId(String agentId, long agentStartTime, long transactionSequence) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        this.agentId = agentId;
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
    }

    public TransactionId(long agentStartTime, long transactionSequence) {
        this.agentStartTime = agentStartTime;
        this.transactionSequence = transactionSequence;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionId that = (TransactionId) o;

        if (agentStartTime != that.agentStartTime) return false;
        if (transactionSequence != that.transactionSequence) return false;
        if (!agentId.equals(that.agentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = agentId.hashCode();
        result = 31 * result + (int) (agentStartTime ^ (agentStartTime >>> 32));
        result = 31 * result + (int) (transactionSequence ^ (transactionSequence >>> 32));
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionId{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", agentStartTime=").append(agentStartTime);
        sb.append(", transactionSequence=").append(transactionSequence);
        sb.append('}');
        return sb.toString();
    }
}
