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

package com.navercorp.pinpoint.otlp.collector.model;

import com.navercorp.pinpoint.common.server.util.StringPrecondition;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class OtlpMetricData {
    private final String tenantId;

    /**
     * otel service.name: ${pinpoint.applicationName}
     */
    @NonNull
    private final String serviceName;
    @NonNull
    private final String agentId;

    private final String metricGroupName;
    @NonNull
    private final String metricName;
    private final String unit;
    private final String version;

    private final MetricType metricType;
    private final AggregationTemporality aggreTemporality;

    private final List<OtlpMetricDataPoint> value;

    public OtlpMetricData(Builder builder) {
        this.tenantId = builder.tenantId;
        this.serviceName = StringPrecondition.requireHasLength(builder.serviceName, "serviceName");
        this.agentId = StringPrecondition.requireHasLength(builder.agentId, "agentId");

        this.metricGroupName = builder.metricGroupName;
        this.metricName = StringPrecondition.requireHasLength(builder.metricName, "metricName");
        this.unit = builder.unit;

        this.metricType = builder.metricType;
        this.aggreTemporality = builder.aggreTemporality;
        this.value = builder.value;
        this.version = builder.version;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getMetricGroupName() {
        return metricGroupName;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getUnit() {
        return unit;
    }

    public int getMetricType() {
        return metricType.getNumber();
    }

    public int getAggreTemporality() {
        return aggreTemporality.getNumber();
    }

    public List<OtlpMetricDataPoint> getValues() {
        return value;
    }

    public static OtlpMetricData.Builder newBuilder() {
        return new OtlpMetricData.Builder();
    }

    public static class Builder {

        private String tenantId;
        private String serviceName;
        private String agentId;

        private String metricGroupName = "";
        private String metricName;
        private String unit = "";
        private String version = "";

        private MetricType metricType = MetricType.GAUGE;
        private AggregationTemporality aggreTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;

        private final List<OtlpMetricDataPoint> value = new ArrayList<>();

        protected Builder() {
        }

        public OtlpMetricData build() {
            return new OtlpMetricData(this);
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = StringPrecondition.requireHasLength(serviceName, "serviceName");
        }

        public void setMetricGroupName(String metricGroupName) {
            this.metricGroupName = metricGroupName;
        }

        public void setMetricName(String metricName) {
            this.metricName = StringPrecondition.requireHasLength(metricName, "metricName");
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public void setMetricType(MetricType metricType) {
            this.metricType = metricType;
        }

        public void setAggreTemporality(AggregationTemporality aggreTemporality) {
            this.aggreTemporality = aggreTemporality;
        }

        public void addValue(OtlpMetricDataPoint value) {
            this.value.add(value);
        }

        public void setAgentId(String agentId) {
            this.agentId = StringPrecondition.requireHasLength(agentId, "agentId");
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    @Override
    public String toString() {
        return "OtlpMetricData{" +
                "tenantId='" + tenantId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", agentId='" + agentId + '\'' +
                ", metricGroupName='" + metricGroupName + '\'' +
                ", metricName='" + metricName + '\'' +
                ", unit='" + unit + '\'' +
                ", metricType='" + metricType + '\'' +
                ", aggreTemporality='" + aggreTemporality + '\'' +
                ", value=" + value + '\'' +
                ", version=" + version + '\'' +
                '}';
    }
}