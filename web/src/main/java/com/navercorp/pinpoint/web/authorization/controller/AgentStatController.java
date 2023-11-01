/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.web.authorization.controller;


import com.navercorp.pinpoint.common.server.bo.stat.AgentStatDataPoint;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.service.stat.AgentStatChartService;
import com.navercorp.pinpoint.web.service.stat.AgentStatService;
import com.navercorp.pinpoint.web.util.FixedTimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSampler;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author emeroad
 * @author minwoo.jung
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping("/getAgentStat/{chartType}")
@Validated
public class AgentStatController<DP extends AgentStatDataPoint> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final Map<String, AgentStatService<DP>> agentStatServiceMap;

    private final Map<String, AgentStatChartService<?>> agentStatChartServiceMap;

    public AgentStatController(
            List<AgentStatService<DP>> agentStatServiceList,
            List<AgentStatChartService<?>> agentStatChartServiceList
    ) {
        this.agentStatServiceMap = buildDispatchMap(agentStatServiceList);
        this.agentStatChartServiceMap = buildDispatchMap(agentStatChartServiceList);
    }

    private <T> Map<String, T> buildDispatchMap(List<T> list) {
        final ChartTypeMappingBuilder<T> mapping = new ChartTypeMappingBuilder<>();

        final Map<String, T> map = mapping.build(list);

        for (final Map.Entry<String, T> entry : map.entrySet()) {
            Class<?> serviceClass = entry.getValue().getClass();
            String chartType = entry.getKey();
            logger.info("chartType:{} {}", chartType, serviceClass.getSimpleName());
        }
        return map;
    }

    private static <T> T getChartService(Map<String, T> map, String chartType) {
        final T service = map.get(chartType);
        if (service == null) {
            throw new IllegalArgumentException("chartType pathVariable not found chartType:" + chartType);
        }
        return service;
    }

    @GetMapping()
    public List<DP> getAgentStat(
            @RequestParam("agentId") @NotBlank String agentId,
            @PathVariable("chartType") @NotBlank String chartType,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range rangeToScan = Range.between(from, to);

        final AgentStatService<DP> agentStatService = getChartService(this.agentStatServiceMap, chartType);
        return agentStatService.selectAgentStatList(agentId, rangeToScan);
    }


    @GetMapping(value = "/chart")
    public StatChart<?> getAgentStatChart(
            @RequestParam("agentId") @NotBlank String agentId,
            @PathVariable("chartType") @NotBlank String chartType,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final TimeWindowSampler sampler = new TimeWindowSlotCentricSampler();
        final TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);

        final AgentStatChartService<?> agentStatChartService =
                getChartService(this.agentStatChartServiceMap, chartType);
        return agentStatChartService.selectAgentChart(agentId, timeWindow);
    }

    @GetMapping(value = "/chart", params = {"interval"})
    public StatChart<?> getAgentStatChart(
            @RequestParam("agentId") @NotBlank String agentId,
            @PathVariable("chartType") @NotBlank String chartType,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam("interval") @PositiveOrZero Integer interval) {
        final int minSamplingInterval = 5;
        final long intervalMs = interval < minSamplingInterval ? minSamplingInterval * 1000L : interval * 1000L;
        final TimeWindowSampler sampler = new FixedTimeWindowSampler(intervalMs);
        final TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);

        final AgentStatChartService<? extends StatChart<?>> agentStatChartService =
                getChartService(this.agentStatChartServiceMap, chartType);
        return agentStatChartService.selectAgentChart(agentId, timeWindow);
    }

    @GetMapping(value = "/chartList")
    public List<? extends StatChart<?>> getAgentStatChartList(
            @RequestParam("agentId") @NotBlank String agentId,
            @PathVariable("chartType") @NotBlank String chartType,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final TimeWindowSampler sampler = new TimeWindowSlotCentricSampler();
        final TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);

        final AgentStatChartService<? extends StatChart<?>> agentStatChartService =
                getChartService(this.agentStatChartServiceMap, chartType);
        return agentStatChartService.selectAgentChartList(agentId, timeWindow);
    }

    @GetMapping(value = "/chartList", params = {"interval"})
    public List<? extends StatChart<?>> getAgentStatChartList(
            @RequestParam("agentId") @NotBlank String agentId,
            @PathVariable("chartType") @NotBlank String chartType,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam("interval") Integer interval) {
        final int minSamplingInterval = 5;
        final long intervalMs = interval < minSamplingInterval ? minSamplingInterval * 1000L : interval * 1000L;
        final TimeWindowSampler sampler = new FixedTimeWindowSampler(intervalMs);
        final TimeWindow timeWindow = new TimeWindow(Range.between(from, to), sampler);

        final AgentStatChartService<?> agentStatChartService =
                getChartService(this.agentStatChartServiceMap, chartType);
        return agentStatChartService.selectAgentChartList(agentId, timeWindow);
    }

}
