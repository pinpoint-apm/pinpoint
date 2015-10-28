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

    public void setSpanBoList(List<SpanBo> spanBoList) {
        this.spanBoList = spanBoList;
    }

    @JsonProperty("metadata")
    public List<MetaData> getMetadata() {
        List<MetaData> list = new ArrayList<MetaData>();
        for (SpanBo span : spanBoList) {
            list.add(new MetaData(span));
        }

        return list;
    }

    @JsonSerialize(using=TransactionMetaDataSerializer.class)
    public static class MetaData {
        private String traceId;
        private long collectorAcceptTime;
        private long startTime;
        private long elapsed;
        private String application;
        private String agentId;
        private String endpoint;
        private int exception;
        private String remoteAddr;
        private String logButtonName;
        private String logPageUrl;

        public MetaData(SpanBo span) {
            traceId = span.getTransactionId();
            collectorAcceptTime = span.getCollectorAcceptTime();
            startTime = span.getStartTime();
            elapsed = span.getElapsed();
            application = span.getRpc();
            agentId = span.getAgentId();
            endpoint = span.getEndPoint();
            exception = span.getErrCode();
            remoteAddr = span.getRemoteAddr();
            logButtonName = "";
            logPageUrl = "";
        }

        public String getTraceId() {
            return traceId;
        }

        public long getCollectorAcceptTime() {
            return collectorAcceptTime;
        }

        public long getStartTime() {
            return startTime;
        }

        public long getElapsed() {
            return elapsed;
        }

        public String getApplication() {
            return application;
        }

        public String getAgentId() {
            return agentId;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public int getException() {
            return exception;
        }

        public String getRemoteAddr() {
            return remoteAddr;
        }

        public String getLogButtonName() {
            return logButtonName;
        }

        public String getLogPageUrl() {
            return logPageUrl;
        }
    }
}
