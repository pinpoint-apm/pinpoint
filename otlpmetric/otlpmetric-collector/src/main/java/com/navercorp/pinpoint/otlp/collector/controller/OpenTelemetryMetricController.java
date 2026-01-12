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

package com.navercorp.pinpoint.otlp.collector.controller;

import com.navercorp.pinpoint.common.server.bo.AgentInfoBo;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.otlp.collector.mapper.OtlpMetricAgentInfoMapper;
import com.navercorp.pinpoint.otlp.collector.mapper.OtlpMetricAgentLifeCycleMapper;
import com.navercorp.pinpoint.otlp.collector.mapper.OtlpMetricMapper;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricData;
import com.navercorp.pinpoint.otlp.collector.model.OtlpMetricDataPoint;
import com.navercorp.pinpoint.otlp.collector.service.HbaseOtlpMetricAgentInfoService;
import com.navercorp.pinpoint.otlp.collector.service.HbaseOtlpMetricAgentLifeCycleService;
import com.navercorp.pinpoint.otlp.collector.service.OtlpMetricCollectorService;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.KeyValue;
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
    @NotNull
    private final OtlpMetricCollectorService otlpMetricCollectorService;
    @NotNull
    private final OtlpMetricMapper otlpMetricMapper;
    private final HbaseOtlpMetricAgentInfoService hbaseOtlpMetricAgentInfoService;
    private final OtlpMetricAgentInfoMapper otlpMetricAgentInfoMapper;
    private final HbaseOtlpMetricAgentLifeCycleService hbaseOtlpMetricAgentLifeCycleService;
    private final OtlpMetricAgentLifeCycleMapper otlpMetricAgentLifeCycleMapper;

    public OpenTelemetryMetricController(TenantProvider tenantProvider,
                                         @Valid OtlpMetricCollectorService otlpMetricCollectorService,
                                         @Valid OtlpMetricMapper otlpMetricDataMapper,
                                         HbaseOtlpMetricAgentInfoService hbaseOtlpMetricAgentInfoService,
                                         OtlpMetricAgentInfoMapper otlpMetricAgentInfoMapper,
                                         HbaseOtlpMetricAgentLifeCycleService hbaseOtlpMetricAgentLifeCycleService,
                                         OtlpMetricAgentLifeCycleMapper otlpMetricAgentLifeCycleMapper) {
        Objects.requireNonNull(tenantProvider, "tenantProvider");
        this.tenantId = tenantProvider.getTenantId();
        this.otlpMetricCollectorService = Objects.requireNonNull(otlpMetricCollectorService, "otlpMetricCollectorService");
        this.otlpMetricMapper = Objects.requireNonNull(otlpMetricDataMapper, "otlpMetricDataMapper");
        this.otlpMetricMapper.setTenantId(tenantId);

        this.hbaseOtlpMetricAgentInfoService = hbaseOtlpMetricAgentInfoService;
        this.otlpMetricAgentInfoMapper = otlpMetricAgentInfoMapper;
        this.hbaseOtlpMetricAgentLifeCycleService = hbaseOtlpMetricAgentLifeCycleService;
        this.otlpMetricAgentLifeCycleMapper = otlpMetricAgentLifeCycleMapper;
    }

    @PostMapping(value = "/opentelemetry", consumes = "application/x-protobuf")
    public ResponseEntity<Void> saveOtlpMetric(@RequestBody ExportMetricsServiceRequest otlp) {
        List<ResourceMetrics> resourceMetricsList = otlp.getResourceMetricsList();

        for (ResourceMetrics resourceMetrics : resourceMetricsList) {
            List<KeyValue> attributesList = resourceMetrics.getResource().getAttributesList();
            Map<String, String> tags = convertToMap(attributesList);
            long agentStartTime = System.currentTimeMillis();

            List<ScopeMetrics> scopeMetricsList = resourceMetrics.getScopeMetricsList();
            for (ScopeMetrics scopeMetrics : scopeMetricsList) {
                List<Metric> metricList = scopeMetrics.getMetricsList();
                for (Metric metric : metricList) {
                    OtlpMetricData metricData = toMetrics(metric, tags);
                    if (metricData != null) {
                        otlpMetricCollectorService.save(metricData);

                        if (logger.isDebugEnabled()) {
                            logger.debug("tenantId:{} serviceName:{} metricGroupName:{} metricName: {}",
                                    metricData.getTenantId(),
                                    metricData.getServiceName(),
                                    metricData.getMetricGroupName(),
                                    metricData.getMetricName());
                        }
                        // find agent start time
                        agentStartTime = findStartTime(metricData);
                    }
                }
            }
            if (agentStartTime > 0) {
                // opentelemetry agent interval option
                // e.g. -Dotel.metric.export.interval=60000
                AgentInfoBo agentInfoBo = null;
                AgentLifeCycleBo agentLifeCycleBo = null;

                try {
                    agentInfoBo = otlpMetricAgentInfoMapper.map(attributesList, agentStartTime);
                    agentLifeCycleBo = otlpMetricAgentLifeCycleMapper.map(agentInfoBo.getAgentId(), agentStartTime);
                } catch (Exception e) {
                    logger.warn("Failed to map", e);
                }

                if (agentInfoBo != null && agentLifeCycleBo != null) {
                    try {
                        hbaseOtlpMetricAgentInfoService.insert(agentInfoBo);
                        hbaseOtlpMetricAgentLifeCycleService.insert(agentLifeCycleBo);
                    } catch (Exception e) {
                        logger.warn("Failed to save", e);
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

    private long findStartTime(OtlpMetricData metricData) {
        for (OtlpMetricDataPoint point : metricData.getValues()) {
            return point.getStartTime();
        }
        return 0;
    }
}