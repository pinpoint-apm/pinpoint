package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.common.bo.SpanBo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class TransactionMetaDataViewModel {
    private List<SpanBo> spanBoList = new ArrayList<SpanBo>();
    private String logButtionName = "";
    private String logPageUrl = "";

    public void setSpanBoList(List<SpanBo> spanBoList) {
        this.spanBoList = spanBoList;
    }

    public void setLogButtionName(String logButtionName) {
        this.logButtionName = logButtionName;
    }

    public void setLogPageUrl(String logPageUrl) {
        this.logPageUrl = logPageUrl;
    }

    @JsonProperty("metadata")
    public List<MetaData> getMetadata() {
        List<MetaData> list = new ArrayList<MetaData>();
        for (SpanBo span : spanBoList) {
            list.add(new MetaData(span, logButtionName, logPageUrl));
        }

        return list;
    }

    //@JsonSerialize(using=TransactionMetaDataSerializer.class)
    public static class MetaData {
        private SpanBo span;
        private String logButtonName = "";
        private String logPageUrl = "";

        public MetaData(SpanBo span, String logButtionName, String logPageUrl) {
            this.span = span;
            this.logButtonName = logButtionName;
            this.logPageUrl = logPageUrl;
        }

        @JsonProperty("traceId")
        public String getTraceId() {
            return span.getTransactionId();
        }

        @JsonProperty("collectorAcceptTime")
        public long getCollectorAcceptTime() {
            return span.getCollectorAcceptTime();
        }

        @JsonProperty("startTime")
        public long getStartTime() {
            return span.getStartTime();
        }

        @JsonProperty("elapsed")
        public long getElapsed() {
            return span.getElapsed();
        }

        @JsonProperty("application")
        public String getApplication() {
            return span.getRpc();
        }

        @JsonProperty("agentId")
        public String getAgentId() {
            return span.getAgentId();
        }

        @JsonProperty("endpoint")
        public String getEndpoint() {
            return span.getEndPoint();
        }

        @JsonProperty("exception")
        public int getException() {
            return span.getErrCode();
        }

        @JsonProperty("remoteAddr")
        public String getRemoteAddr() {
            return span.getRemoteAddr();
        }

        @JsonProperty("logButtonName")
        public String getLogButtonName() {
            return logButtonName;
        }

        @JsonProperty("logPageUrl")
        public String getLogPageUrl() {
            return logPageUrl;
        }
    }
}
