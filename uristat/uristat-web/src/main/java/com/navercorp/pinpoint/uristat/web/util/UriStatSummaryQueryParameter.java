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

import com.navercorp.pinpoint.common.server.util.EnumGetter;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.security.InvalidParameterException;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class UriStatSummaryQueryParameter extends QueryParameter {
    private final String tenantId;
    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final OrderBy orderBy;
    private final String isDesc;

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public String getIsDesc() {
        return isDesc;
    }

    public boolean isApplicationStat() {
        return StringUtils.isEmpty(agentId);
    }

    private enum OrderBy {
        URI("uri"),
        APDEX("apdex", "(apdexRaw / totalCount)"),
        TOTAL("totalCount"),
        FAILURE("failureCount"),
        MAX("maxTimeMs"),
        AVG("avgTimeMs", "(totalTimeMs / totalCount)");

        private final String name;
        private final String desc;

        private static final EnumGetter<OrderBy> GETTER = new EnumGetter<>(OrderBy.class);

        OrderBy(String name) {
            this.name = name;
            this.desc = name;
        }

        OrderBy(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public static OrderBy fromValue(String name) {
            return GETTER.fromValue(OrderBy::getName, name);
        }

        @Override
        public String toString() {
            return desc;
        }
    }

    protected UriStatSummaryQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.tenantId = builder.tenantId;
        this.serviceName = builder.serviceName;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.orderBy = builder.orderBy;
        this.isDesc = builder.isDesc;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {
        private String tenantId;
        private String serviceName;
        private String applicationName;
        private String agentId;
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

        public Builder setOrderby(String orderBy) throws InvalidParameterException {
            UriStatSummaryQueryParameter.OrderBy order = OrderBy.fromValue(orderBy);
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
        public UriStatSummaryQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }

            if (limit == 0) {
                this.limit = estimateLimit();
            }

            return new UriStatSummaryQueryParameter(this);
        }
    }
}
