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

import com.navercorp.pinpoint.exceptiontrace.web.model.GroupByAttributes;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
public class ExceptionTraceQueryParameter extends QueryParameter {

    public static final int STACKTRACE_COUNT = 3;
    private final static String TOTAL_FIELD_NAME = "total";

    private final String applicationName;
    private final String agentId;

    private final String transactionId;
    private final long spanId;
    private final long exceptionId;
    private final int exceptionDepth;

    private final List<GroupByAttributes> groupByAttributes;

    private final long timeWindowRangeCount;

    protected ExceptionTraceQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.applicationName = builder.applicationName;
        this.agentId = builder.agentId;
        this.transactionId = builder.transactionId;
        this.spanId = builder.spanId;
        this.exceptionId = builder.exceptionId;
        this.exceptionDepth = builder.exceptionDepth;
        this.groupByAttributes = builder.groupByAttributes;
        this.timeWindowRangeCount = builder.timeWindowRangeCount;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {
        private static final int LIMIT = 65536;
        private String applicationName;
        private String agentId = null;


        private String transactionId = null;
        private long spanId = Long.MIN_VALUE;
        private long exceptionId = Long.MIN_VALUE;
        private int exceptionDepth = Integer.MIN_VALUE;

        private final List<GroupByAttributes> groupByAttributes = new ArrayList<>();

        private long timeWindowRangeCount = 0;

        @Override
        protected Builder self() {
            return this;
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
            this.agentId = agentId;
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

        public Builder addAllGroupBies(Collection<GroupByAttributes> summaryGroupBIES) {
            List<String> attributes = summaryGroupBIES.stream().map(GroupByAttributes::getAttributeName).collect(Collectors.toList());

            this.groupByAttributes.addAll(
                    summaryGroupBIES
            );
            return self();
        }

        public long estimateLimit() {
            if (this.range != null) {
                return (range.getRange() / Math.max(timePrecision.getInterval(), 30000) + 1);
            } else {
                return LIMIT;
            }
        }

        @Override
        public ExceptionTraceQueryParameter build() {
            if (timePrecision == null) {
                this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, 30000);
            }
            this.limit = this.estimateLimit();
            return new ExceptionTraceQueryParameter(this);
        }
    }

    @Override
    public String toString() {
        return "ExceptionTraceQueryParameter{" +
                "applicationName='" + applicationName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", spanId=" + spanId +
                ", exceptionId=" + exceptionId +
                ", exceptionDepth=" + exceptionDepth +
                ", groupByAttributes=" + groupByAttributes +
                ", timeWindowRangeCount=" + timeWindowRangeCount +
                '}';
    }
}
