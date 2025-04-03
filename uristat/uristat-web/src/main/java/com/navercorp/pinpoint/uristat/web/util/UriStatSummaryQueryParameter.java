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

import com.google.common.primitives.Ints;
import com.navercorp.pinpoint.common.server.util.EnumGetter;
import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.common.timeseries.window.TimePrecision;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;

import java.security.InvalidParameterException;
import java.util.concurrent.TimeUnit;

public class UriStatSummaryQueryParameter extends QueryParameter {
    @Deprecated
    private final String tenantId;

    private final String serviceName;
    private final String applicationName;
    private final String agentId;
    private final OrderBy orderBy;
    private final String isDesc;
    private final long tenTimesLimit;

    /**
     * @deprecated Since 3.1.0
     */
    @Deprecated
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
        APDEX("apdex", "(apdexRaw / totalCount)", "(totalApdexRaw / totalCount)"),
        TOTAL("totalCount"),
        FAILURE("failureCount"),
        MAX("maxTimeMs"),
        AVG("avgTimeMs", "(totalTimeMs / totalCount)", "(sumOfTotalTimeMs / totalCount)");

        private final String name;
        private final String desc;
        private String optional;

        private static final EnumGetter<OrderBy> GETTER = new EnumGetter<>(OrderBy.class);

        OrderBy(String name) {
            this.name = name;
            this.desc = name;
            this.optional = name;
        }

        OrderBy(String name, String desc) {
            this.name = name;
            this.desc = desc;
            this.optional = desc;
        }

        OrderBy(String name, String desc, String optional) {
            this.name = name;
            this.desc = desc;
            this.optional = optional;
        }

        public String getName() {
            return name;
        }

        public static OrderBy fromValue(String name) {
            return GETTER.fromValueIgnoreCase(OrderBy::getName, name);
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
        this.applicationName = StringPrecondition.requireHasLength(builder.applicationName, "applicationName");
        this.agentId = builder.agentId;
        this.orderBy = builder.orderBy;
        this.isDesc = builder.isDesc;
        this.tenTimesLimit = builder.getLimit() * 10;
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

        /**
         * @deprecated Since 3.1.0
         */
        @Deprecated
        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return self();
        }
        public Builder setServiceName(String serviceName) {
            this.serviceName = serviceName;
            return self();
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = StringPrecondition.requireHasLength(applicationName, "applicationName");
            return self();
        }

        public Builder setAgentId(String agentId) {
            if (StringUtils.hasLength(agentId)) {
                this.agentId = agentId;
            }
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

        public Builder setLimit(int limit) {
            this.limit = Ints.constrainToRange(limit, 50, 200);
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
