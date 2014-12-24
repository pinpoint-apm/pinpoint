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

package com.navercorp.pinpoint.web.controller;


import com.navercorp.pinpoint.web.applicationmap.link.MatcherGroup;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.AgentStatService;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.util.TimeWindowSlotCentricSampler;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.linechart.agentstat.AgentStatChartGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author emeroad
 * @author minwoo.jung
 */
@Controller
public class AgentStatController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentStatService agentStatService;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired(required = false)
    private MatcherGroup matcherGroup;

    @RequestMapping(value = "/getAgentStat", method = RequestMethod.GET)
    @ResponseBody
    public AgentStatChartGroup getAgentStat(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "sampleRate", required = false) Integer sampleRate) throws Exception {
        StopWatch watch = new StopWatch();
        watch.start("agentStatService.selectAgentStatList");
        TimeWindow timeWindow = new TimeWindow(new Range(from, to), new TimeWindowSlotCentricSampler());
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range rangeToScan = new Range(scanFrom, scanTo);
        List<AgentStat> agentStatList = agentStatService.selectAgentStatList(agentId, rangeToScan);
        watch.stop();

        if (logger.isInfoEnabled()) {
            logger.info("getAgentStat(agentId={}, from={}, to={}) : {}ms", agentId, from, to, watch.getLastTaskTimeMillis());
        }

        // FIXME dummy
//        int nPoints = (int) (to - from) / 5000;
//        if (sampleRate == null) {
//            sampleRate = nPoints < 300 ? 1 : nPoints / 300;
//        }

        AgentStatChartGroup chartGroup = new AgentStatChartGroup(timeWindow);
        chartGroup.addAgentStats(agentStatList);
        chartGroup.buildCharts();

        return chartGroup;
    }

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET)
    @ResponseBody
    public ApplicationAgentList getApplicationAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = new Range(from, to);
        return new ApplicationAgentList(agentInfoService.getApplicationAgentList(applicationName, range), matcherGroup);
    }
}
