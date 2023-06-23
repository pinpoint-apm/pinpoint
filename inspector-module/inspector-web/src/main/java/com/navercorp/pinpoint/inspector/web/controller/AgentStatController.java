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
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricGroupDataVeiw;
import com.navercorp.pinpoint.inspector.web.view.InspectorMetricView;
import com.navercorp.pinpoint.metric.web.util.Range;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.metric.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.metric.web.view.SystemMetricView;
import com.navercorp.pinpoint.pinot.tenant.TenantProvider;
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
public class AgentStatController {

    AgentStatService agentStatChartService;

    private final TimeWindowSampler DEFAULT_TIME_WINDOW_SAMPLER = new TimeWindowSlotCentricSampler(5000L, 200);
    private final TenantProvider tenantProvider;

    public AgentStatController(AgentStatService agentStatChartService, TenantProvider tenantProvider) {
        this.agentStatChartService = Objects.requireNonNull(agentStatChartService, "agentStatChartService");
        this.tenantProvider = Objects.requireNonNull(tenantProvider, "tenantProvider");
    }

    // TODO : (minwoo) tenantId should be considered. The collector side should also be considered.
    @GetMapping(value = "/chart")
    public InspectorMetricView getAgentStatChart(
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, agentId, metricDefinitionId, timeWindow);

        InspectorMetricData inspectorMetricData =  agentStatChartService.selectAgentStat(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricView(inspectorMetricData);
    }

    @GetMapping(value = "/chartList")
    public InspectorMetricGroupDataVeiw getAgentStatChartList(
            @RequestParam("agentId") String agentId,
            @RequestParam("metricDefinitionId") String metricDefinitionId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        String tenantId = tenantProvider.getTenantId();
        TimeWindow timeWindow = new TimeWindow(Range.newRange(from, to), DEFAULT_TIME_WINDOW_SAMPLER);
        InspectorDataSearchKey inspectorDataSearchKey = new InspectorDataSearchKey(tenantId, agentId, metricDefinitionId, timeWindow);

        InspectorMetricGroupData inspectorMetricGroupData = agentStatChartService.selectAgentStatWithGrouping(inspectorDataSearchKey, timeWindow);
        return new InspectorMetricGroupDataVeiw(inspectorMetricGroupData);
    }
}
