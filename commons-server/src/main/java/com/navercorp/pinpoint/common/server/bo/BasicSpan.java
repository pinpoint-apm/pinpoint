package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.util.TransactionId;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface BasicSpan {

    String getAgentId();
    void setAgentId(String agentId);

    String getApplicationId();
    void  setApplicationId(String applicationId);

    long getAgentStartTime();
    void setAgentStartTime(long agentStartTime);

    long getSpanId();
    void setSpanId(long spanId);

    TransactionId getTransactionId();
//    void setTransactionId(TransactionId transactionId);


//    List<SpanEventBo> getSpanEventBoList();
}
