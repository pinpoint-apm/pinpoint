/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.exceptiontrace.web.util;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.exceptiontrace.web.ExceptionTraceWebConfig;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import org.springframework.beans.factory.annotation.Value;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
public class ExceptionTraceQueryParameter extends QueryParameter {

    private final boolean errorMessageClpEnabled;
    private final String tableName;

    private final String tenantId;
    private final String applicationName;
    private final String agentId;

    private final String transactionId;
    private final long spanId;
    private final long exceptionId;
    private final int exceptionDepth;

    private final OrderByAttributes orderBy;
    private final String isDesc;
    private final List<GroupByAttributes> groupByAttributes;

    private final long timeWindowRangeCount;

    protected ExceptionTraceQueryParameter(
            Builder builder
    ) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.errorMessageClpEnabled = builder.errorMessageClpEnabled;
        this.tableName = builder.tableName;
        this.tenantId = builder.tenantId;
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.transactionId = builder.transactionId;
        this.spanId = builder.spanId;
        this.exceptionId = builder.exceptionId;
        this.exceptionDepth = builder.exceptionDepth;
        this.orderBy = builder.orderBy;
        this.isDesc = builder.isDesc;
        this.groupByAttributes = builder.groupByAttributes;
        this.timeWindowRangeCount = builder.timeWindowRangeCount;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {

        private static final int MAX_LIMIT = 65536;
        private Integer hardLimit = null;

        private boolean errorMessageClpEnabled;
        private String tableName;

        private String tenantId;
        private String applicationName;
        private String agentId = null;


        private String transactionId = null;
        private long spanId = Long.MIN_VALUE;
        private long exceptionId = Long.MIN_VALUE;
        private int exceptionDepth = Integer.MIN_VALUE;

        private OrderByAttributes orderBy;
        private String isDesc;
        private final List<GroupByAttributes> groupByAttributes = new ArrayList<>();

        private long timeWindowRangeCount = 0;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setErrorMessageClpEnabled(boolean errorMessageClpEnabled) {
            this.errorMessageClpEnabled = errorMessageClpEnabled;
            return self();
        }

        public Builder setTableName(String tableName) {
            this.tableName = tableName;
            return self();
        }

        public Builder setTenantId(String tenantId) {
            this.tenantId = tenantId;
            return self();
        }

        public Builder setApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return self();
        }

        public Builder setExceptionDepth(int exceptionDepth) {
            this.exceptionDepth = exceptionDepth;
            return self();
        }

        public Builder setAgentId(String agentId) {
            if (StringUtils.hasLength(agentId)) {
                this.agentId = agentId;
            }
            return self();
        }

        public Builder setTransactionId(String transactionId) {
            this.transactionId = transactionId;
            return self();
        }

        public Builder setSpanId(long spanId) {
            this.spanId = spanId;
            return self();
        }

        public Builder setExceptionId(long exceptionId) {
            this.exceptionId = exceptionId;
            return self();
        }

        public Builder setTimeWindowRangeCount(long timeWindowRangeCount) {
            this.timeWindowRangeCount = timeWindowRangeCount;
            return self();
        }

        public Builder setHardLimit(int limit) {
            if (limit > 200) {
                this.hardLimit = 200;
            } else this.hardLimit = Math.max(limit, 50);
            return self();
        }

        public Builder setOrderBy(String orderBy) {
            OrderByAttributes order = OrderByAttributes.fromValue(orderBy);
            if (order == null) {
                throw new InvalidParameterException("Invalid order by type : " + orderBy + " not supported.");
            }
            this.orderBy = order;
            return self();
        }

        public Builder setIsDesc(boolean desc) {
            if (desc) {
                this.isDesc = "desc";
            } else {
                this.isDesc = "asc";
            }
            return self();
        }

        public Builder addAllGroupByList(Collection<String> strings) {
            if (strings == null) {
                return self();
            }
            List<GroupByAttributes> groupByAttributesList = strings.stream().map(
                            GroupByAttributes::fromValue
                    )
                    .filter(Objects::nonNull)
                    .distinct().sorted().collect(Collectors.toList());
            this.groupByAttributes.addAll(groupByAttributesList);
            return self();
        }

        public long useLimitIfSet() {
            if (hardLimit != null) {
                return hardLimit;
            }
            return estimateLimit();
        }

        @Override
        public long estimateLimit() {
            if (this.range != null) {
                return (range.getRange() / Math.max(timePrecision.getInterval(), 30000) + 1);
            } else {
                return MAX_LIMIT;
            }
        }

        @Override
        public ExceptionTraceQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }
            this.limit = this.useLimitIfSet();
            return new ExceptionTraceQueryParameter(this);
        }
    }

    @Override
    public String toString() {
        return "ExceptionTraceQueryParameter{" +
                "tenantId='" + tenantId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", spanId=" + spanId +
                ", exceptionId=" + exceptionId +
                ", exceptionDepth=" + exceptionDepth +
                ", orderBy=" + orderBy +
                ", isDesc='" + isDesc + '\'' +
                ", groupByAttributes=" + groupByAttributes +
                ", timeWindowRangeCount=" + timeWindowRangeCount +
                '}';
    }
}
