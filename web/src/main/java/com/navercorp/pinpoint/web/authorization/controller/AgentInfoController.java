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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.view.tree.SimpleTreeView;
import com.navercorp.pinpoint.web.view.tree.TreeNode;
import com.navercorp.pinpoint.web.view.tree.TreeView;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilters;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.InstancesList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
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
@Validated
public class AgentInfoController {

    private final AgentInfoService agentInfoService;
    private final AgentEventService agentEventService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final SortByAgentInfo.Rules DEFAULT_SORT_BY = SortByAgentInfo.Rules.AGENT_ID_ASC;

    public AgentInfoController(
            AgentInfoService agentInfoService,
            AgentEventService agentEventService,
            ServiceTypeRegistryService serviceTypeRegistryService
    ) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    @GetMapping(value = "/getAgentList", params = {"!application"})
    public TreeView<TreeNode<AgentAndStatus>> getAgentList() {
        final long timestamp = System.currentTimeMillis();
        return getAgentList(timestamp);
    }

    @GetMapping(value = "/getAgentList", params = {"!application", "from", "to"})
    public TreeView<TreeNode<AgentAndStatus>> getAgentList(
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final AgentStatusFilter filter = AgentStatusFilters.recentRunning(from);
        final AgentsMapByApplication<AgentAndStatus> allAgentsList =
                this.agentInfoService.getAllAgentsList(filter, Range.between(from, to));
        return treeView(allAgentsList);
    }


    @GetMapping(value = "/getAgentList", params = {"!application", "timestamp"})
    public TreeView<TreeNode<AgentAndStatus>> getAgentList(
            @RequestParam("timestamp") @PositiveOrZero long timestamp) {
        final AgentsMapByApplication<AgentAndStatus> allAgentsList =
                this.agentInfoService.getAllAgentsList(AgentStatusFilters.acceptAll(), Range.between(timestamp, timestamp));
        return treeView(allAgentsList);
    }

    private static TreeView<TreeNode<AgentAndStatus>> treeView(AgentsMapByApplication<AgentAndStatus> agentsListsList) {
        final List<InstancesList<AgentAndStatus>> list = agentsListsList.getAgentsListsList();
        return new SimpleTreeView<>(list, InstancesList::getGroupName, InstancesList::getInstancesList);
    }

    @GetMapping(value = "/getAgentList", params = {"application"})
    public TreeView<TreeNode<AgentStatusAndLink>> getAgentList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName
    ) {
        final long timestamp = System.currentTimeMillis();
        return getAgentList(applicationName, serviceTypeName, timestamp);
    }

    @GetMapping(value = "/getAgentList", params = {"application", "from", "to"})
    public TreeView<TreeNode<AgentStatusAndLink>> getAgentList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final AgentStatusFilter currentRunFilter = AgentStatusFilters.recentRunning(from);
        final ServiceType serviceType = findServiceTypeByName(serviceTypeName);
        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                currentRunFilter,
                AgentInfoFilters.acceptAll(),
                applicationName,
                serviceType.getCode(),
                Range.between(from, to),
                DEFAULT_SORT_BY
        );
        return treeView(list);
    }

    @GetMapping(value = "/getAgentList", params = {"application", "timestamp"})
    public TreeView<TreeNode<AgentStatusAndLink>> getAgentList(
            @RequestParam("application") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("timestamp") @PositiveOrZero long timestamp) {
        final ServiceType serviceType = findServiceTypeByName(serviceTypeName);
        final AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(
                AgentStatusFilters.running(),
                AgentInfoFilters.acceptAll(),
                applicationName,
                serviceType.getCode(),
                Range.between(timestamp, timestamp),
                DEFAULT_SORT_BY
        );
        return treeView(list);
    }

    private static TreeView<TreeNode<AgentStatusAndLink>> treeView(AgentsMapByHost agentsMapByHost) {
        final List<InstancesList<AgentStatusAndLink>> list = agentsMapByHost.getAgentsListsList();
        return new SimpleTreeView<>(list, InstancesList::getGroupName, InstancesList::getInstancesList);
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
        return this.agentEventService.getAgentEvents(agentId, range, excludeEventTypes);
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
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        final Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range);
    }

    @GetMapping(value = "/getAgentStatusTimeline", params = {"exclude"})
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("agentId") @NotBlank String agentId,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        final Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range, excludeEventTypeCodes);
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

    private ServiceType findServiceTypeByName(String serviceTypeName) {
        if (serviceTypeName == null) {
            return PinpointConstants.DEFAULT_SERVICE_TYPE;
        }
        return this.serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
    }

}
