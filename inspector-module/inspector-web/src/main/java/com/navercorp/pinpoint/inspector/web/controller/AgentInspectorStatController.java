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

import com.navercorp.pinpoint.common.server.util.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.server.util.time.RangeValidator;
import com.navercorp.pinpoint.inspector.web.config.InspectorWebProperties;
import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.AgentStatService;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindow;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSampler;
import com.navercorp.pinpoint.common.server.util.timewindow.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
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
    @GetMapping(value = "/chart")
    public InspectorMetricView getAgentStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow);

        InspectorMetricData inspectorMetricData = agentStatService.selectAgentStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chart", params = "metricDefinitionId=apdex")
    public InspectorMetricView getApdexStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("serviceTypeName") @NotBlank String serviceTypeName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        InspectorMetricData inspectorMetricData = apdexStatService.selectAgentStat(applicationName, serviceTypeName, metricDefinitionId, agentId, from, to);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataView getAgentStatChartList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        rangeValidator.validate(range.getFromInstant(), range.getToInstant());

        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = getTimeWindow(range);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatService.selectAgentStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }

    private TimeWindow getTimeWindow(Range range) {
        return new TimeWindow(range, DEFAULT_TIME_WINDOW_SAMPLER);
    }
}
