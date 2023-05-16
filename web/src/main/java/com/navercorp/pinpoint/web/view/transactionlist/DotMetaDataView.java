package com.navercorp.pinpoint.web.view.transactionlist;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface DotMetaDataView {

    @JsonProperty("traceId")
    String getTraceId();

    @JsonProperty("collectorAcceptTime")
    long getCollectorAcceptTime();

    @JsonProperty("startTime")
    long getStartTime();

    @JsonProperty("elapsed")
    long getElapsed();

    @JsonProperty("application")
    String getApplication();

    @JsonProperty("agentId")
    String getAgentId();

    @JsonProperty("agentName")
    String getAgentName();

    @JsonProperty("endpoint")
    String getEndpoint();

    @JsonProperty("exception")
    int getException();

    @JsonProperty("remoteAddr")
    String getRemoteAddr();

    @JsonProperty("spanId")
    String getSpanId();
}