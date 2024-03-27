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

import com.navercorp.pinpoint.inspector.web.model.InspectorDataSearchKey;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricData;
import com.navercorp.pinpoint.inspector.web.model.InspectorMetricGroupData;
import com.navercorp.pinpoint.inspector.web.service.AgentStatService;
import com.navercorp.pinpoint.inspector.web.service.ApdexStatService;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataView;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.metric.common.model.Range;
import com.navercorp.pinpoint.metric.common.model.TimeWindow;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.common.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping("/getAgentStatV2")
public class AgentStatV2Controller {

    AgentStatService agentStatService;

    ApdexStatService apdexStatService;

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(5000L, 200);
    private final TenantProvider tenantProvider;

    public AgentStatV2Controller(AgentStatService agentStatService, ApdexStatService apdexStatService, TenantProvider tenantProvider) {
        this.agentStatService = Objects.requireNonNull(agentStatService, "agentStatService");
        this.apdexStatService = Objects.requireNonNull(apdexStatService, "apdexStatService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    // TODO : (minwoo) tenantId should be considered. The collector side should also be considered.
    @GetMapping(value = "/chart")
    public InspectorMetricView getAgentStatChart(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow, version);

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
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        InspectorMetricData inspectorMetricData = apdexStatService.selectAgentStat(applicationName, serviceTypeName, metricDefinitionId, agentId, from, to);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataView getAgentStatChartList(
            @RequestParam("applicationName") String applicationName,
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value="version", defaultValue="2") int version) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, applicationName, agentId, metricDefinitionId, timeWindow, version);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatService.selectAgentStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataView(inspectorMetricGroupData);
    }
}
