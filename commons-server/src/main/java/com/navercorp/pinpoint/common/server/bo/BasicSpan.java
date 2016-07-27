package com.navercorp.pinpoint.common.server.bo;

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

    String getTraceAgentId();
    long getTraceAgentStartTime();
    long getTraceTransactionSequence();


//    List<SpanEventBo> getSpanEventBoList();
}
