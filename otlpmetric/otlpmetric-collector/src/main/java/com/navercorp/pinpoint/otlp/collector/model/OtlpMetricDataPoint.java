/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.model;

import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import io.opentelemetry.proto.metrics.v1.DataPointFlags;

import java.util.HashMap;
import java.util.Map;

public class OtlpMetricDataPoint {
    private final Long eventTime;
    private final Long startTime;
    private final DataType dataType;
    private final AggreFunc aggreFunc;
    private final DataPointFlags flags;
    private final String fieldName;
    private final String description;
    private final Number value;
    private final Map<String, String> tags;

    public OtlpMetricDataPoint(Builder builder) {
        this.eventTime = builder.eventTime;
        this.startTime = builder.startTime;
        this.dataType = builder.dataType;
        this.aggreFunc = builder.aggreFunc;
        this.flags = builder.flags;
        this.fieldName = builder.fieldName;
        this.description = builder.description;
        this.value = builder.value;
        this.tags = builder.tags;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getAggreFunc() {
        return aggreFunc.getNumber();
    }

    public int getFlag() {
        return flags.getNumber();
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDescription() {
        return description;
    }

    public Number getValue() {
        return value;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public static class Builder {
        private Long eventTime;
        private Long startTime = null;
        private DataType dataType = DataType.DOUBLE;
        private AggreFunc aggreFunc = AggreFunc.AVERAGE;
        private DataPointFlags flags = DataPointFlags.UNRECOGNIZED;
        private String fieldName = "";
        private String description = "";
        private Number value;
        private final Map<String, String> tags = new HashMap<>();

        public OtlpMetricDataPoint build() {
            return new OtlpMetricDataPoint(this);
        }


        public Builder setFieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder setDataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder setAggreFunc(AggreFunc aggreFunc) {
            this.aggreFunc = aggreFunc;
            return this;
        }

        public Builder setFlags(DataPointFlags flags) {
            this.flags = flags;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder addTags(String key, String value) {
            tags.put(key, value);
            return this;
        }

        public Builder addTags(Map<String, String> tags) {
            this.tags.putAll(tags);
            return this;
        }

        public Builder setValue(Number value) {
            this.value = value;
            return this;
        }

        public Builder setEventTime(long eventTime) {
            if (eventTime != 0L) {
                this.eventTime = eventTime;
            } else {
                this.eventTime = null;
            }
            return this;
        }

        public Builder setStartTime(long startTime) {
            if (startTime != 0L) {
                this.startTime = startTime;
            } else {
                this.startTime = null;
            }
            return this;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OtlpMetricDataPoint{");
        sb.append("fieldName='").append(fieldName).append('\'');
        sb.append(", dataType='").append(dataType).append('\'');
        sb.append(", aggreFunc='").append(aggreFunc).append('\'');
        sb.append(", flags='").append(flags).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", tags='").append(tags).append('\'');
        sb.append(", value=").append(value).append('\'');
        sb.append(", eventTime=").append(eventTime).append('\'');
        sb.append(", startTime=").append(startTime).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
