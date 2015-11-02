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

    //@JsonSerialize(using=TransactionMetaDataSerializer.class)
    public static class MetaData {
        private SpanBo span;

        public MetaData(SpanBo span) {
            this.span = span;
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
    }
}
