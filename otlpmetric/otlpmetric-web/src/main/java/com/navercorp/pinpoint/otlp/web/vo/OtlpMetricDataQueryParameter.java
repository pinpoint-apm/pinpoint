/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.vo;

import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.util.TimePrecision;
import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.model.DataType;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author minwoo-jung
 */
public class OtlpMetricDataQueryParameter extends QueryParameter {
    private final String serviceId;
    private final String applicationId;
    private final String agentId;
    private final String metricGroupName;
    private final String metricName;
    private final String fieldName;
    private final List<String> tags;
    private final String version;
    private final AggregationFunction aggregationFunction;
    private final int dataType;
    private final TimeWindow timeWindow;

    public DataType getDataType() {
        return DataType.forNumber(dataType);
    }

    protected OtlpMetricDataQueryParameter(Builder builder) {
        super(builder.getRange(), builder.getTimePrecision(), builder.getLimit());
        this.serviceId = builder.serviceId;
        this.applicationId = builder.applicationId;
        this.agentId = builder.agentId;
        this.metricGroupName = builder.metricGroupName;
        this.metricName = builder.metricName;
        this.fieldName = builder.fieldName;
        this.tags = List.of(builder.tags.split(","));
        this.aggregationFunction = builder.aggregationFunction;
        this.dataType = builder.dataType;
        this.version = builder.version;
        this.timeWindow = builder.timeWindow;
    }

    public static class Builder extends QueryParameter.Builder<Builder> {
        private String serviceId;
        private String applicationId;
        private String agentId;
        private String metricGroupName;
        private String metricName;
        private String fieldName;
        private String tags;
        private String version;
        private AggregationFunction aggregationFunction;
        private int dataType;
        private TimeWindow timeWindow;

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setServiceId(String serviceId) {
            this.serviceId = serviceId;
            return self();
        }

        public Builder setApplicationId(String applicationId) {
            this.applicationId = applicationId;
            return self();
        }

        public Builder setAgentId(String agentId) {
            this.agentId = agentId;
            return self();
        }

        public Builder setMetricGroupName(String metricGroupName) {
            this.metricGroupName = metricGroupName;
            return self();
        }

        public Builder setMetricName(String metricName) {
            this.metricName = metricName;
            return self();
        }

        public Builder setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return self();
        }

        public Builder setTags(String tags) {
            this.tags = tags;
            return self();
        }

        public Builder setVersion(String version) {
            this.version = version;
            return self();
        }

        public Builder setAggregationFunction(AggregationFunction aggregationFunction) {
            this.aggregationFunction = aggregationFunction;
            return self();
        }

        public Builder setDataType(DataType dataType) {
            this.dataType = dataType.getNumber();
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

        public Builder setTimeWindow(TimeWindow timeWindow) {
            this.timeWindow = timeWindow;
            this.range = timeWindow.getWindowRange();
            this.timeSize = (int) timeWindow.getWindowSlotSize();
            this.timePrecision = TimePrecision.newTimePrecision(TimeUnit.MILLISECONDS, (int) timeWindow.getWindowSlotSize());
            this.limit = timeWindow.getWindowRangeCount();
            return self();
        }

        @Override
        public OtlpMetricDataQueryParameter build() {
            if (timeWindow == null) {
                throw new InvalidParameterException("TimeWindow is required.");
            }

            return new OtlpMetricDataQueryParameter(this);
        }
    }

    @Override
    public String toString() {
        return "OtlpMetricDataQueryParameter{" +
                "serviceId='" + serviceId + '\'' +
                ", applicationId='" + applicationId + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricGroupName='" + metricGroupName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", fieldName='" + fieldName + '\'' +
                ", tags=" + tags +
                ", version='" + version + '\'' +
                ", aggregationFunction=" + aggregationFunction +
                ", dataType=" + dataType +
                ", TimePrecision=" + timePrecision +
                ", range=" + range.prettyToString() +
                ", range(from)=" + range.getFrom() +
                ", range(to)=" + range.getTo() +
                ", limit=" + limit +
                '}';
    }
}
