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

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.otlp.collector.mapper.OtlpMappingException;
import com.navercorp.pinpoint.otlp.common.model.MetricType;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import jakarta.validation.constraints.NotBlank;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OtlpMetricData {
    private final String tenantId;
    private final ServiceId serviceNamespace;
    @NotBlank private final ApplicationId serviceName;
    @NotBlank private final String agentId;

    private final String metricGroupName;
    @NotBlank private final String metricName;
    private final String unit;
    private final String version;

    private final MetricType metricType;
    private final AggregationTemporality aggreTemporality;

    private final List<OtlpMetricDataPoint> value;

    public OtlpMetricData(Builder builder) {
        this.tenantId = builder.tenantId;
        this.serviceNamespace = builder.serviceNamespace;
        this.serviceName = builder.serviceName;
        this.agentId = builder.agentId;

        this.metricGroupName = builder.metricGroupName;
        this.metricName = builder.metricName;
        this.unit = builder.unit;

        this.metricType = builder.metricType;
        this.aggreTemporality = builder.aggreTemporality;
        this.value = builder.value;
        this.version = builder.version;
    }

    public String getTenantId() {
        return tenantId;
    }

    public ServiceId getServiceNamespace() {
        return serviceNamespace;
    }

    public ApplicationId getServiceName() {
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

    public static class Builder {
        private final Logger logger = LogManager.getLogger(this.getClass());
        private String tenantId;
        private ServiceId serviceNamespace = ServiceId.DEFAULT_ID;
        private ApplicationId serviceName;
        private String agentId;

        private String metricGroupName = "";
        private String metricName;
        private String unit = "";
        private String version = "";

        private MetricType metricType = MetricType.GAUGE;
        private AggregationTemporality aggreTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;

        private List<OtlpMetricDataPoint> value = new ArrayList<>();


        public OtlpMetricData build() {
            return new OtlpMetricData(this);
        }

        public void setTenantId(String tenantId) {
            this.tenantId = tenantId;
        }

        public void setServiceNamespace(String serviceNamespace) {
            try {
                this.serviceNamespace = ServiceId.of(UUID.fromString(serviceNamespace));
            } catch (IllegalArgumentException e) {
                this.serviceNamespace = ServiceId.DEFAULT_ID;
                logger.info("Otlp metric service.namespace doesn't match Pinpoint serviceId format (UUID). service.namespace:{}", serviceNamespace);
            }
        }
        public void setServiceName(String serviceName) {
            try {
                this.serviceName = ApplicationId.of(UUID.fromString(serviceName));
            } catch (IllegalArgumentException e) {
                throw new OtlpMappingException("Resource attribute `service.name` doesn't match Pinpoint applicationId format (UUID). service.name:" + serviceName);
            }
        }

        public void setMetricGroupName(String metricGroupName) {
            this.metricGroupName = metricGroupName;
        }

        public void setMetricName(String metricName) {
            this.metricName = metricName;
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
            this.agentId = agentId;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OtlpMetricData{");
        sb.append("tenantId='").append(tenantId).append('\'');
        sb.append(", serviceNamespace='").append(serviceNamespace).append('\'');
        sb.append(", serviceName='").append(serviceName).append('\'');
        sb.append(", agentId='").append(agentId).append('\'');
        sb.append(", metricGroupName='").append(metricGroupName).append('\'');
        sb.append(", metricName='").append(metricName).append('\'');
        sb.append(", unit='").append(unit).append('\'');
        sb.append(", metricType='").append(metricType).append('\'');
        sb.append(", aggreTemporality='").append(aggreTemporality).append('\'');
        sb.append(", value=").append(value).append('\'');
        sb.append(", version=").append(version).append('\'');
        sb.append('}');
        return sb.toString();
    }
}