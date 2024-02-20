/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.collector.controller;

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.otlp.collector.mapper.OtlpMetricMapper;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricDataPoint;
import com.navercorp.pinpoint.otlp.collector.service.OtlpMetricCollectorService;
import com.navercorp.pinpoint.otlp.common.model.AggreFunc;
import com.navercorp.pinpoint.otlp.common.model.DataType;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.DataPointFlags;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class OpenTelemetryMetricController {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String tenantId;
    @NotNull private final OtlpMetricCollectorService otlpMetricCollectorService;
    @NotNull private final OtlpMetricMapper otlpMetricMapper;

    public OpenTelemetryMetricController(TenantProvider tenantProvider,
                                         @Valid OtlpMetricCollectorService otlpMetricCollectorService,
                                         @Valid OtlpMetricMapper otlpMetricDataMapper) {
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.tenantId = tenantProvider.getTenantId();
        this.otlpMetricCollectorService = Objects.requireNonNull(otlpMetricCollectorService, "otlpMetricService");
        this.otlpMetricMapper = Objects.requireNonNull(otlpMetricDataMapper, "otlpMetricDataMapper");
        this.otlpMetricMapper.setTenantId(tenantId);
    }

    @PostMapping(value = "/opentelemetry", consumes = "application/x-protobuf")
    public ResponseEntity<Void> saveOtlpMetric(@RequestBody ExportMetricsServiceRequest otlp)  {
        List<ResourceMetrics> resourceMetricsList = otlp.getResourceMetricsList();

        for (ResourceMetrics resourceMetrics : resourceMetricsList) {
            List<KeyValue> attributesList = resourceMetrics.getResource().getAttributesList();
            Map<String, String> tags = convertToMap(attributesList);

            List<ScopeMetrics> scopeMetricsList = resourceMetrics.getScopeMetricsList();
            for (ScopeMetrics scopeMetrics : scopeMetricsList) {
                List<Metric> metricList = scopeMetrics.getMetricsList();
                for (Metric metric: metricList) {
                    OtlpMetricData metricData = toMetrics(metric, tags);
                    if (metricData != null) {
                        otlpMetricCollectorService.save(metricData);

                        if (logger.isDebugEnabled()) {
                            logger.debug("tenantId:{} serviceNamespace:{} serviceName:{} metricGroupName:{} metricName: {}",
                                    metricData.getTenantId(),
                                    metricData.getServiceNamespace(),
                                    metricData.getServiceName(),
                                    metricData.getMetricGroupName(),
                                    metricData.getMetricName());
                        }
                    }
                }
            }
        }

    return ResponseEntity.ok().build();
    }

    private OtlpMetricData toMetrics(Metric metric, Map<String, String> tags) {
        OtlpMetricData otlpMetricData = otlpMetricMapper.map(metric, tags);
        return otlpMetricData;
    }

    private Map<String, String> convertToMap(List<KeyValue> tags) {
        Map<String, String> tagMap = new HashMap<>();
        for (KeyValue tag : tags) {
            tagMap.put(tag.getKey(), tag.getValue().getStringValue());
        }
        return tagMap;
    }
}