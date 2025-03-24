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

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.component.AgentEventQuery;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
@RestController
@RequestMapping("/api")
@Validated
public class AgentInfoController {
    private final AgentInfoService agentInfoService;

    private final AgentEventService agentEventService;

    public AgentInfoController(AgentInfoService agentInfoService, AgentEventService agentEventService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
    }

    @GetMapping(value = "/getAgentInfo")
    public AgentAndStatus getAgentInfo(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") @PositiveOrZero long timestamp) {
        return this.agentInfoService.getAgentInfo(agentId, timestamp);
    }

    @GetMapping(value = "/getDetailedAgentInfo")
    public DetailedAgentAndStatus getDetailedAgentInfo(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") @PositiveOrZero long timestamp) {
        return this.agentInfoService.getDetailedAgentInfo(agentId, timestamp);
    }

    @GetMapping(value = "/getAgentStatus")
    public AgentStatus getAgentStatus(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") @PositiveOrZero long timestamp) {
        return this.agentInfoService.getAgentStatus(agentId, timestamp);
    }

    @GetMapping(value = "/getAgentEvent")
    public AgentEvent getAgentEvent(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("eventTimestamp") @PositiveOrZero long eventTimestamp,
            @RequestParam("eventTypeCode") int eventTypeCode
    ) {
        final AgentEventType eventType = AgentEventType.getTypeByCode(eventTypeCode);
        if (eventType == null) {
            throw new IllegalArgumentException("invalid eventTypeCode [" + eventTypeCode + "]");
        }

        return this.agentEventService.getAgentEvent(agentId, eventTimestamp, eventType);
    }

    @GetMapping(value = "/getAgentEvents")
    public List<AgentEvent> getAgentEvents(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        final Range range = Range.between(from, to);
        final Set<AgentEventType> excludeEventTypes = getAgentEventTypes(excludeEventTypeCodes);
        AgentEventQuery exclude = AgentEventQuery.exclude(excludeEventTypes);
        return this.agentEventService.getAgentEvents(agentId, range, exclude);
    }

    private static Set<AgentEventType> getAgentEventTypes(int[] excludeEventTypeCodes) {
        final Set<AgentEventType> excludeEventTypes = EnumSet.noneOf(AgentEventType.class);
        for (final int excludeEventTypeCode : excludeEventTypeCodes) {
            final AgentEventType excludeEventType = AgentEventType.getTypeByCode(excludeEventTypeCode);
            if (excludeEventType != null) {
                excludeEventTypes.add(excludeEventType);
            }
        }
        return excludeEventTypes;
    }

    @GetMapping(value = "/getAgentStatusTimeline")
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(applicationName, agentId, range);
    }

    @GetMapping(value = "/getAgentStatusTimeline", params = {"exclude"})
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        final Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(applicationName, agentId, range, excludeEventTypeCodes);
    }

    @RequestMapping(value = "/isAvailableAgentId")
    public CodeResult<String> isAvailableAgentId(@RequestParam("agentId") @NotBlank String agentId) {
        final IdValidateUtils.CheckResult result = IdValidateUtils.checkId(agentId, PinpointConstants.AGENT_ID_MAX_LEN);
        if (result == IdValidateUtils.CheckResult.FAIL_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "length range is 1 ~ 24");
        }
        if (result == IdValidateUtils.CheckResult.FAIL_PATTERN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "invalid pattern(" + IdValidateUtils.ID_PATTERN_VALUE + ")"
            );
        }
        if (agentInfoService.isExistAgentId(agentId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "agentId already exists");
        }
        return CodeResult.ok("OK");
    }

}
