/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.web.service.stat.LegacyAgentStatChartService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.chart.LegacyAgentStatChartGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Deprecated agent stat controller only to preserve backwards compatibility until
 * testing is done for agent stat v2, and the front-end API is changed.
 *
 * @author HyunGil Jeong
 */
@Deprecated
@Controller
public class LegacyAgentStatController {

    private static final int MAX_RESPONSE_SIZE = 200;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("legacyAgentStatChartServiceFactory")
    private LegacyAgentStatChartService agentStatService;

    @Autowired
    @Qualifier("legacyAgentStatChartV1Service")
    private LegacyAgentStatChartService v1Service;

    @Autowired
    @Qualifier("legacyAgentStatChartV2Service")
    private LegacyAgentStatChartService v2Service;

    @Deprecated
    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentStat", method = RequestMethod.GET)
    @ResponseBody
    public LegacyAgentStatChartGroup getAgentStat(
        @RequestParam("agentId") String agentId,
        @RequestParam("from") long from,
        @RequestParam("to") long to,
        @RequestParam(value = "sampleRate", required = false) Integer sampleRate) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start("agentStatService.selectAgentStatList");
        TimeWindow timeWindow = new TimeWindow(new Range(from, to), new TimeWindowSlotCentricSampler());
        LegacyAgentStatChartGroup chartGroup = this.agentStatService.selectAgentStatList(agentId, timeWindow);
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("getAgentStat(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
        }
        return chartGroup;
    }

    @Deprecated
    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentStat/v1", method = RequestMethod.GET)
    @ResponseBody
    public LegacyAgentStatChartGroup getAgentStatV1(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start("agentStatService.selectAgentStatList");
        TimeWindow timeWindow = new TimeWindow(new Range(from, to), new TimeWindowSlotCentricSampler());
        LegacyAgentStatChartGroup chartGroup = this.v1Service.selectAgentStatList(agentId, timeWindow);
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("getAgentStatV1(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
        }
        return chartGroup;
    }

    @Deprecated
    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentStat/v2", method = RequestMethod.GET)
    @ResponseBody
    public LegacyAgentStatChartGroup getAgentStatV2(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start("agentStatService.selectAgentStatList");
        TimeWindow timeWindow = new TimeWindow(new Range(from, to), new TimeWindowSlotCentricSampler());
        LegacyAgentStatChartGroup chartGroup = this.v2Service.selectAgentStatList(agentId, timeWindow);
        watch.stop();
        if (logger.isInfoEnabled()) {
            logger.info("getAgentStatV2(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
        }
        return chartGroup;
    }
}
