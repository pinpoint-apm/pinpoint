package com.navercorp.pinpoint.common.server.bo.serializer.trace.v1;

import com.navercorp.pinpoint.common.util.TransactionId;

/**
 * @author Woonduk Kang(emeroad)
 */
public class SpanDecodingContext {

    private String agentId;
    private String applicationId;
    private long agentStartTime;

    private long spanId;
    private long collectorAcceptedTime;
    private TransactionId transactionId;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public long getAgentStartTime() {
        return agentStartTime;
    }

    public void setAgentStartTime(long agentStartTime) {
        this.agentStartTime = agentStartTime;
    }

    public long getSpanId() {
        return spanId;
    }

    public void setSpanId(long spanId) {
        this.spanId = spanId;
    }

    public void setCollectorAcceptedTime(long collectorAcceptedTime) {
        this.collectorAcceptedTime = collectorAcceptedTime;
    }

    public long getCollectorAcceptedTime() {
        return collectorAcceptedTime;
    }

    public TransactionId getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(TransactionId transactionId) {
        this.transactionId = transactionId;
    }

    public void next() {

    }

    public void finish() {
    }
}
