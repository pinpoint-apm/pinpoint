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
import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.component.AgentEventQuery;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import com.navercorp.pinpoint.service.web.resolver.ServiceParam;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
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
public class AgentInfoController implements AccessDeniedExceptionHandler {
    private final AgentInfoService agentInfoService;

    private final AgentEventService agentEventService;

    private final RangeValidator rangeValidator;

    public AgentInfoController(AgentInfoService agentInfoService, AgentEventService agentEventService, ConfigProperties configProperties) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        Objects.requireNonNull(configProperties, "configProperties");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(configProperties.getInspectorPeriodMax()));
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #timestamp))")
    @GetMapping(value = "/getAgentInfo")
    public AgentAndStatus getAgentInfo(
            @ServiceParam ServiceName serviceName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") Timestamp timestamp) {
        AgentAndStatus result = this.agentInfoService.findAgentInfoAndStatus(agentId, timestamp.getEpochMillis());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "agent info not found");
        }
        return result;
    }

    @GetMapping(value = "/getDetailedAgentInfo")
    public DetailedAgentAndStatus getDetailedAgentInfo(
            @ServiceParam ServiceName serviceName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") Timestamp timestamp) {
        DetailedAgentAndStatus result = this.agentInfoService.findDetailedAgentInfoAndStatus(agentId, timestamp.getEpochMillis());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "detailed agent info not found");
        }
        return result;
    }

    @GetMapping(value = "/getAgentStatus")
    public AgentStatus getAgentStatus(
            @ServiceParam ServiceName serviceName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("timestamp") Timestamp timestamp) {
        AgentStatus result = this.agentInfoService.findAgentStatus(agentId, timestamp.getEpochMillis());
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "agent status not found");
        }
        return result;
    }

    @GetMapping(value = "/getAgentEvent")
    public AgentEvent getAgentEvent(
            @ServiceParam ServiceName serviceName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("eventTimestamp") Timestamp eventTimestamp,
            @RequestParam("eventTypeCode") int eventTypeCode
    ) {
        final AgentEventType eventType = AgentEventType.getTypeByCode(eventTypeCode);
        if (eventType == null) {
            throw new IllegalArgumentException("invalid eventTypeCode [" + eventTypeCode + "]");
        }

        AgentEvent result = this.agentEventService.getAgentEvent(agentId, eventTimestamp.getEpochMillis(), eventType);
        if (result == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "agent event not found");
        }
        return result;
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to))")
    @GetMapping(value = "/getAgentEvents")
    public List<AgentEvent> getAgentEvents(
            @ServiceParam ServiceName serviceName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to,
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

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to))")
    @GetMapping(value = "/getAgentStatusTimeline")
    public InspectorTimeline getAgentStatusTimeline(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        final Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(applicationName, agentId, range);
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to))")
    @GetMapping(value = "/getAgentStatusTimeline", params = {"exclude"})
    public InspectorTimeline getAgentStatusTimeline(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        final Range range = Range.between(from, to);
        rangeValidator.validate(range);
        return agentInfoService.getAgentStatusTimeline(applicationName, agentId, range, excludeEventTypeCodes);
    }

    @RequestMapping(value = "/isAvailableAgentId")
    public CodeResult<String> isAvailableAgentId(@ServiceParam ServiceName serviceName, @RequestParam("agentId") @NotBlank String agentId) {
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
        if (agentInfoService.findAgentInfo(agentId, System.currentTimeMillis()) != null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "agentId already exists");
        }
        return CodeResult.ok("OK");
    }

}
