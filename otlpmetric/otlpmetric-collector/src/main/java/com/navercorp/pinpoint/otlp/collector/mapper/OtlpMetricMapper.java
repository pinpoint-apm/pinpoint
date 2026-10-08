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
import com.navercorp.pinpoint.otlp.collector.model.OtlpResource;
import com.navercorp.pinpoint.otlp.collector.model.OtlpResourceAttributes;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Metric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class OtlpMetricMapper {
    private final Logger logger = LogManager.getLogger(this.getClass());
    // read per resource, not at construction: the provider may resolve the tenant per request
    private final TenantProvider tenantProvider;
    @NonNull
    private final OtlpMetricDataMapper[] mappers;

    public OtlpMetricMapper(TenantProvider tenantProvider, OtlpMetricDataMapper[] mappers) {
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.mappers = Objects.requireNonNull(mappers);
        for (OtlpMetricDataMapper mapper : mappers) {
            logger.info("MicrometerMetricsDataMapper:{}", mapper.getClass().getSimpleName());
        }
    }

    /**
     * Parses the resource attributes once per ResourceMetrics.
     *
     * @return null when a required attribute is missing; every metric of the resource is skipped then
     */
    @Nullable
    public OtlpResource mapResource(List<KeyValue> attributes) {
        Objects.requireNonNull(attributes, "attributes");
        try {
            return parseResource(tenantProvider.getTenantId(), attributes);
        } catch (OtlpMappingException ex) {
            logger.info("Failed saving OTLP metrics: {}", ex.getMessage());
            return null;
        }
    }

    private OtlpResource parseResource(@Nullable String tenantId, List<KeyValue> attributes) {
        String serviceName = null;
        String serviceNamespace = null;
        String agentId = null;
        String version = null;
        Map<String, String> tags = new HashMap<>();
        for (KeyValue attribute : attributes) {
            String key = attribute.getKey();
            String value = AttributeValues.toString(attribute.getValue());
            switch (key) {
                case OtlpResourceAttributes.KEY_SERVICE_NAME:
                    serviceName = value;
                    break;
                case OtlpResourceAttributes.KEY_SERVICE_NAMESPACE:
                    serviceNamespace = value;
                    break;
                case OtlpResourceAttributes.KEY_PINPOINT_AGENTID:
                    agentId = value;
                    break;
                case OtlpResourceAttributes.KEY_PINPOINT_METRIC_VERSION:
                    version = value;
                    break;
                default:
                    tags.put(key, value);
            }
        }

        if (!StringUtils.hasText(serviceName)) {
            throw new OtlpMappingException("Resource attribute `service.name` is required to save OTLP metrics to Pinpoint.");
        }
        if (!StringUtils.hasText(agentId)) {
            throw new OtlpMappingException("Resource attribute `pinpoint.agentId` is required to save OTLP metrics to Pinpoint");
        }
        if (!StringUtils.hasText(serviceNamespace)) {
            serviceNamespace = null;
        }
        if (!StringUtils.hasText(version)) {
            version = "";
        }
        return new OtlpResource(tenantId, serviceName, serviceNamespace, agentId, version, tags);
    }

    @Nullable
    public OtlpMetricData map(Metric metric, OtlpResource resource) {
        if (metric == null) {
            return null;
        }
        Objects.requireNonNull(resource, "resource");

        final OtlpMetricData.Builder builder = OtlpMetricData.newBuilder();

        builder.setTenantId(resource.getTenantId());
        builder.setUnit(metric.getUnit());

        builder.setServiceNamespace(resource.getServiceNamespace());
        builder.setServiceName(resource.getServiceName());
        builder.setAgentId(resource.getAgentId());
        builder.setVersion(resource.getVersion());

        try {
            this.map(builder, metric, resource.getTags());
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
}
