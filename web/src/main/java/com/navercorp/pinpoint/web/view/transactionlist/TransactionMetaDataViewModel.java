/*
 * Copyright 2025 NAVER Corp.
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
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.server.bo.SpanBo;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * @author jaehong.kim
 */
public class TransactionMetaDataViewModel {
    private final List<SpanBo> spanBoList;
    private final IntFunction<String> serviceTypeNameResolver;

    public TransactionMetaDataViewModel() {
        this(Collections.emptyList(), code -> null);
    }

    public TransactionMetaDataViewModel(List<SpanBo> spanBoList, IntFunction<String> serviceTypeNameResolver) {
        this.spanBoList = Objects.requireNonNull(spanBoList, "spanBoList");
        this.serviceTypeNameResolver = Objects.requireNonNull(serviceTypeNameResolver, "serviceTypeNameResolver");
    }

    @JsonProperty("metadata")
    public List<MetaData> getMetadata() {
        return Lists.transform(spanBoList, span -> new MetaData(span, serviceTypeNameResolver));
    }

    public static class MetaData implements DotMetaDataView {
        private final SpanBo span;
        private final IntFunction<String> serviceTypeNameResolver;

        public MetaData(SpanBo span, IntFunction<String> serviceTypeNameResolver) {
            this.span = span;
            this.serviceTypeNameResolver = serviceTypeNameResolver;
        }

        // applicationName/serviceType identify the application the span belongs to so a
        // consumer can build application-scoped routes (e.g. /transactionList/{name}@{type})
        // from a bare traceId lookup. The "application" field stays rpc for compatibility.
        @JsonProperty("applicationName")
        public String getApplicationName() {
            return span.getApplicationName();
        }

        @JsonProperty("serviceType")
        public String getServiceTypeName() {
            return serviceTypeNameResolver.apply(span.getApplicationServiceType());
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
            return span.getStartTimeMillis();
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
