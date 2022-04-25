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

import com.navercorp.pinpoint.web.response.ErrorResponse;
import com.navercorp.pinpoint.web.response.Response;
import com.navercorp.pinpoint.web.response.SuccessResponse;
import com.navercorp.pinpoint.web.service.AgentStatisticsService;
import com.navercorp.pinpoint.web.util.DateTimeUtils;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.common.server.util.time.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@RestController
public class AgentStatisticsController {

    private final AgentStatisticsService agentStatisticsService;

    public AgentStatisticsController(AgentStatisticsService agentStatisticsService) {
        this.agentStatisticsService = Objects.requireNonNull(agentStatisticsService, "agentStatisticsService");
    }

    @GetMapping(value = "/insertAgentCount", params = {"agentCount"})
    public ResponseEntity<Response> insertAgentCount(@RequestParam("agentCount") int agentCount) {
        return insertAgentCount(agentCount, new Date().getTime());
    }

    @GetMapping(value = "/insertAgentCount", params = {"agentCount", "timestamp"})
    public ResponseEntity<Response> insertAgentCount(@RequestParam("agentCount") int agentCount, @RequestParam("timestamp") long timestamp) {
        if (timestamp < 0) {
            return ErrorResponse.badRequest("negative timestamp.");
        }

        AgentCountStatistics agentCountStatistics = new AgentCountStatistics(agentCount, DateTimeUtils.timestampToStartOfDay(timestamp));
        boolean success = agentStatisticsService.insertAgentCount(agentCountStatistics);

        if (success) {
            return SuccessResponse.ok();
        } else {
            return ErrorResponse.serverError("insert DAO error.");
        }
    }

    @GetMapping(value = "/selectAgentCount")
    public List<AgentCountStatistics> selectAgentCount() {
        return selectAgentCount(0L, System.currentTimeMillis());
    }

    @GetMapping(value = "/selectAgentCount", params = {"to"})
    public List<AgentCountStatistics> selectAgentCount(@RequestParam("to") long to) {
        return selectAgentCount(0L, to);
    }

    @GetMapping(value = "/selectAgentCount", params = {"from", "to"})
    public List<AgentCountStatistics> selectAgentCount(@RequestParam("from") long from, @RequestParam("to") long to) {
        Range range = Range.between(DateTimeUtils.timestampToStartOfDay(from), DateTimeUtils.timestampToStartOfDay(to));
        List<AgentCountStatistics> agentCountStatisticsList = agentStatisticsService.selectAgentCount(range);

        agentCountStatisticsList.sort(Comparator.comparingLong(AgentCountStatistics::getTimestamp).reversed());

        return agentCountStatisticsList;
    }


}
