/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.web.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;

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

    public static class MetaData {
        private SpanBo span;

        public MetaData(SpanBo span) {
            this.span = span;
        }

        @JsonProperty("traceId")
        public String getTraceId() {
            return TransactionIdUtils.formatString(span.getTransactionId());
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

        @JsonProperty("spanId")
        public String getSpanId() {
            return Long.toString(span.getSpanId());
        }
    }
}
