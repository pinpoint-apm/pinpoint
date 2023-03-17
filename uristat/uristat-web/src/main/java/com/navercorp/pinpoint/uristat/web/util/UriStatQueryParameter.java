/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.uristat.web.util;

import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

public class UriStatQueryParameter extends QueryParameter {
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final String uri;
    private final OrderBy orderBy;
    private final String isDesc;


    private enum OrderBy {
        URI("uri"),
        APDEX("apdex"),
        TOTAL("totalCount"),
        FAILURE("failureCount"),
        MAX("maxTimeMs"),
        AVG("avgTimeMs");

        private final String value;

        OrderBy(String value) {
            this.value = value;
        }

        public static OrderBy fromValue(String value) {
            for (OrderBy orderBy : OrderBy.values()) {
                if (orderBy.value.equalsIgnoreCase(value)) {
                    return orderBy;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            switch(this) {
                case APDEX:
                    return "(apdexRaw / totalCount)";
                case AVG:
                    return "(totalTimeMs / totalCount)";
                default:
                    return this.value;
            }
        }
    }

    protected UriStatQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.tenantId = builder.tenantId;
        this.serviceName = builder.serviceName;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.uri = builder.uri;
        this.orderBy = builder.orderBy;
        this.isDesc = builder.isDesc;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {
        private String tenantId;
        private String serviceName;
        private String applicationName;
        private String agentId;
        private String uri;
        private OrderBy orderBy;
        private String isDesc;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return self();
        }
        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return self();
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return self();
        }

        public Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return self();
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return self();
        }

        public Builder setOrderby(String orderBy) throws InvalidParameterException {
            UriStatQueryParameter.OrderBy order = OrderBy.fromValue(orderBy);
            if (order == null) {
                throw new InvalidParameterException("Invalid order by type : " + orderBy + " not supported.");
            }
            this.orderBy = order;
            return self();
        }

        public Builder setDesc(boolean desc) {
            if (desc) {
                this.isDesc = "desc";
            } else {
                this.isDesc = "asc";
            }
            return self();
        }

        public Builder setLimit(long limit) {
            if (limit > 200) {
                this.limit = 200;
            } else if (limit < 50) {
                this.limit = 50;
            } else {
                this.limit = limit;
            }
            return self();
        }

        @Override
        public UriStatQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }

            if (limit == 0) {
                this.limit = estimateLimit();
            }

            return new UriStatQueryParameter(this);
        }
    }
}
