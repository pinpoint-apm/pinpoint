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
package com.navercorp.pinpoint.web.view.transactionlist;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class TransactionMetaDataViewModel {
    private final List<SpanBo> spanBoList;

    public TransactionMetaDataViewModel() {
        this(Collections.emptyList());
    }

    public TransactionMetaDataViewModel(List<SpanBo> spanBoList) {
        this.spanBoList = Objects.requireNonNull(spanBoList, "spanBoList");
    }

    @JsonProperty("metadata")
    public List<MetaData> getMetadata() {
        List<MetaData> list = new ArrayList<>(spanBoList.size());
        for (SpanBo span : spanBoList) {
            list.add(new MetaData(span));
        }

        return list;
    }

    public static class MetaData implements DotMetaDataView {
        private final SpanBo span;

        public MetaData(SpanBo span) {
            this.span = span;
        }

        @Override
        public String getTraceId() {
            return span.getTransactionId().toString();
        }

        @Override
        public long getCollectorAcceptTime() {
            return span.getCollectorAcceptTime();
        }

        @Override
        public long getStartTime() {
            return span.getStartTime();
        }

        @Override
        public long getElapsed() {
            return span.getElapsed();
        }

        @Override
        public String getApplication() {
            return span.getRpc();
        }

        @Override
        public String getAgentId() {
            return span.getAgentId();
        }

        @Override
        public String getAgentName() {
            return span.getAgentName();
        }

        @Override
        public String getEndpoint() {
            return span.getEndPoint();
        }

        @Override
        public int getException() {
            return span.getErrCode();
        }

        @Override
        public String getRemoteAddr() {
            return span.getRemoteAddr();
        }

        @Override
        public String getSpanId() {
            return Long.toString(span.getSpanId());
        }
    }
}
