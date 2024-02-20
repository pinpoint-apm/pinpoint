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

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import io.opentelemetry.proto.metrics.v1.Metric;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
public class OtlpMetricMapper {
    private static final String KEY_SERVICE_NAME = "service.name";
    private static final String KEY_SERVICE_NAMESPACE = "service.namespace";
    private static final String KEY_PINPOINT_AGENTID = "pinpoint.agentId";
    private static final String KEY_PINPOINT_METRIC_VERSION = "pinpoint.metric.version";
    private final Logger logger = LogManager.getLogger(this.getClass());
    private String tenantId = "";
    @NotNull private final OtlpMetricDataMapper[] mappers;

    public OtlpMetricMapper(TenantProvider tenantProvider, OtlpMetricDataMapper[] mappers) {
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.tenantId = tenantProvider.getTenantId();
        this.mappers = Objects.requireNonNull(mappers);
        for (OtlpMetricDataMapper mapper : mappers) {
            logger.info("MicrometerMetricsDataMapper:{}", mapper.getClass().getSimpleName());
        }
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public OtlpMetricData map(Metric metric, Map<String, String> commonTags) {
        if (metric == null) {
            return null;
        }

        final OtlpMetricData.Builder builder = new OtlpMetricData.Builder();
        try {
            parseCommonTags(builder, commonTags);
        } catch (OtlpMappingException ex) {
            logger.info("Failed saving OTLP metric {}: {}", metric.getName(), ex.getMessage());
            return null;
        }

        builder.setTenantId(tenantId);
        builder.setUnit(metric.getUnit());

        this.map(builder, metric, commonTags);
        return builder.build();
    }

    private void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        for (OtlpMetricDataMapper mapper : mappers) {
            mapper.map(builder, metric, commonTags);
        }
    }

    private void parseCommonTags(OtlpMetricData.Builder builder, Map<String, String> commonTags) {
        String serviceNamespace = commonTags.get(KEY_SERVICE_NAMESPACE);
        if (serviceNamespace != null) {
            builder.setServiceNamespace(serviceNamespace);
        }

        String serviceName = commonTags.get(KEY_SERVICE_NAME);
        if (serviceName == null) {
            throw new OtlpMappingException("Resource attribute `service.name` is required to save OTLP metrics to Pinpoint.");
        }
        builder.setServiceName(serviceName);

        String agentId= commonTags.get(KEY_PINPOINT_AGENTID);
        if (agentId == null) {
            throw new OtlpMappingException("Resource attribute `pinpoint.agentId` is required to save OTLP metrics to Pinpoint");
        }

        String version = commonTags.get(KEY_PINPOINT_METRIC_VERSION);
        if (version != null) {
            builder.setVersion(version);
        }

        builder.setAgentId(agentId);
    }
}
