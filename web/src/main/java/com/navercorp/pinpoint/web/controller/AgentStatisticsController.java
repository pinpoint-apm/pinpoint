/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.service.AgentStatisticsService;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
@Controller
public class AgentStatisticsController {

    @Autowired
    AgentStatisticsService agentStatisticsService;

    @RequestMapping(value = "/insertAgentCount", method = RequestMethod.GET, params = {"agentCount"})
    @ResponseBody
    public Map<String, String> insertAgentCount(@RequestParam("agentCount") int agentCount) {
        return insertAgentCount(agentCount, new Date().getTime());
    }

    @RequestMapping(value = "/insertAgentCount", method = RequestMethod.GET, params = {"agentCount", "timestamp"})
    @ResponseBody
    public Map<String, String> insertAgentCount(@RequestParam("agentCount") int agentCount, @RequestParam("timestamp") long timestamp) {
        if (timestamp < 0) {
            Map<String, String> result = new HashMap<>();
            result.put("result", "FAIL");
            result.put("message", "negative timestamp.");
            return result;
        }

        AgentCountStatistics agentCountStatistics = new AgentCountStatistics(agentCount, DateUtils.timestampToMidNight(timestamp));
        boolean success = agentStatisticsService.insertAgentCount(agentCountStatistics);

        if (success) {
            Map<String, String> result = new HashMap<>();
            result.put("result", "SUCCESS");
            return result;
        } else {
            Map<String, String> result = new HashMap<>();
            result.put("result", "FAIL");
            result.put("message", "insert DAO error.");
            return result;
        }
    }

    @RequestMapping(value = "/selectAgentCount", method = RequestMethod.GET)
    @ResponseBody
    public List<AgentCountStatistics> selectAgentCount() {
        return selectAgentCount(0L, System.currentTimeMillis());
    }

    @RequestMapping(value = "/selectAgentCount", method = RequestMethod.GET, params = {"to"})
    @ResponseBody
    public List<AgentCountStatistics> selectAgentCount(@RequestParam("to") long to) {
        return selectAgentCount(0L, to);
    }

    @RequestMapping(value = "/selectAgentCount", method = RequestMethod.GET, params = {"from", "to"})
    @ResponseBody
    public List<AgentCountStatistics> selectAgentCount(@RequestParam("from") long from, @RequestParam("to") long to) {
        Range range = new Range(DateUtils.timestampToMidNight(from), DateUtils.timestampToMidNight(to), true);
        List<AgentCountStatistics> agentCountStatisticsList = agentStatisticsService.selectAgentCount(range);

        Collections.sort(agentCountStatisticsList, new Comparator<AgentCountStatistics>() {
            @Override
            public int compare(AgentCountStatistics o1, AgentCountStatistics o2) {
                if (o1.getTimestamp() > o2.getTimestamp()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });

        return agentCountStatisticsList;
    }

}
