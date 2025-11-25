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

package com.navercorp.pinpoint.otlp.collector.mapper;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpResourceAttributes;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class OtlpMetricMapper {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private String tenantId = "";
    @NonNull
    private final OtlpMetricDataMapper[] mappers;

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

    public OtlpMetricData map(Metric metric, Map<String, String> tags) {
        if (metric == null) {
            return null;
        }

        final OtlpMetricData.Builder builder = OtlpMetricData.newBuilder();

        builder.setTenantId(tenantId);
        builder.setUnit(metric.getUnit());

        try {
            Map<String, String> commonTags = parseCommonTags(builder, tags);
            this.map(builder, metric, commonTags);
        } catch (OtlpMappingException ex) {
            logger.info("Failed saving OTLP metric {}: {}", metric.getName(), ex.getMessage());
            return null;
        }

        return builder.build();
    }

    private void map(OtlpMetricData.Builder builder, Metric metric, Map<String, String> commonTags) {
        for (OtlpMetricDataMapper mapper : mappers) {
            mapper.map(builder, metric, commonTags);
        }
    }

    private Map<String, String> parseCommonTags(OtlpMetricData.Builder builder, Map<String, String> tags) {
        Map<String, String> commonTags = new HashMap<>(tags);

        String serviceName = commonTags.remove(OtlpResourceAttributes.KEY_SERVICE_NAME);
        if (StringUtils.isEmpty(serviceName)) {
            throw new OtlpMappingException("Resource attribute `service.name` is required to save OTLP metrics to Pinpoint.");
        }
        builder.setServiceName(serviceName);

        String agentId = commonTags.remove(OtlpResourceAttributes.KEY_PINPOINT_AGENTID);
        if (StringUtils.isEmpty(serviceName)) {
            throw new OtlpMappingException("Resource attribute `pinpoint.agentId` is required to save OTLP metrics to Pinpoint");
        }

        builder.setAgentId(agentId);

        String version = commonTags.remove(OtlpResourceAttributes.KEY_PINPOINT_METRIC_VERSION);
        if (StringUtils.isEmpty(version) == false) {
            builder.setVersion(version);
        }

        return commonTags;
    }
}
