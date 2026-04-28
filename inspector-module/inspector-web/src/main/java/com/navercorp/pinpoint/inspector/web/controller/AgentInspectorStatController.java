/*
 * Copyright 2023 NAVER Corp.
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

package com.navercorp.pinpoint.inspector.web.controller;

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSampler;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.AgentStatService;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import com.navercorp.pinpoint.service.web.resolver.ServiceParam;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.vo.Service;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping("/api/inspector/agentStat")
public class AgentInspectorStatController {

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(10000L, 200);
    private final AgentStatService agentStatService;
    private final ApdexStatService apdexStatService;
    private final TenantProvider tenantProvider;
    private final RangeValidator rangeValidator;

    public AgentInspectorStatController(AgentStatService agentStatService, ApdexStatService apdexStatService, TenantProvider tenantProvider, InspectorWebProperties inspectorWebProperties) {
        this.agentStatService = Objects.requireNonNull(agentStatService, "agentStatService");
        this.apdexStatService = Objects.requireNonNull(apdexStatService, "apdexStatService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
        Objects.requireNonNull(inspectorWebProperties, "inspectorWebProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(inspectorWebProperties.getInspectorPeriodMax()));
    }

    // TODO : (minwoo) tenantId should be considered. The collector side should also be considered.
    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chart")
    public InspectorMetricView getAgentStatChart(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow);

        InspectorMetricData inspectorMetricData = agentStatService.selectAgentStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chart", params = "metricDefinitionId=apdex")
    public InspectorMetricView getApdexStatChart(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        InspectorMetricData inspectorMetricData = apdexStatService.selectAgentStat(Service.DEFAULT, applicationName, serviceTypeName, metricDefinitionId, agentId, from.getEpochMillis(), to.getEpochMillis());
        return new InspectorMetricView(inspectorMetricData);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataView getAgentStatChartList(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatService.selectAgentStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chart", params = {"agentIds", "metricDefinitionId=apdex"})
    public InspectorMetricGroupDataView getApdexStatChartGroupedByAgentId(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("agentIds") @NotEmpty List<String> agentIds,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        InspectorMetricGroupData inspectorMetricGroupData = apdexStatService.selectAgentStatGroupedByAgentId(
                Service.DEFAULT, applicationName, serviceTypeName, metricDefinitionId, agentIds, from.getEpochMillis(), to.getEpochMillis()
        );
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chart", params = {"agentIds"})
    public InspectorMetricGroupDataView getAgentStatChartGroupedByAgentId(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentIds") @NotEmpty List<String> agentIds,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatService.selectAgentStatGroupedByAgentId(
                tenantId, applicationName, agentIds, metricDefinitionId, timeWindow
        );
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/chartList", params = {"agentIds"})
    public InspectorMetricGroupDataView getAgentStatChartListGroupedByAgentId(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentIds") @NotEmpty List<String> agentIds,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatService.selectAgentStatGroupedByAgentId(
                tenantId, applicationName, agentIds, metricDefinitionId, timeWindow
        );
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    private TimeWindow getTimeWindow(Range range) {
        return new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER);
    }
}
