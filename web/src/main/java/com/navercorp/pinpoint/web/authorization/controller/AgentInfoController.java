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
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.tree.AgentsList;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilterChain;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.DefaultAgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import com.navercorp.pinpoint.web.view.tree.SimpleTreeView;
import com.navercorp.pinpoint.web.view.tree.TreeView;
import org.springframework.http.HttpStatus;
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
public class AgentInfoController {
    private final AgentInfoService agentInfoService;

    private final AgentEventService agentEventService;

    public AgentInfoController(AgentInfoService agentInfoService, AgentEventService agentEventService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
    }

    @GetMapping(value = "/getAgentList", params = {"!application"})
    public TreeView<AgentStatusAndLink> getAgentList() {
        long timestamp = System.currentTimeMillis();
        return getAgentList(timestamp);
    }

    @GetMapping(value = "/getAgentList", params = {"!application", "from", "to"})
    public TreeView<AgentStatusAndLink> getAgentList(
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        AgentInfoFilter filter = new DefaultAgentInfoFilter(from);
        long timestamp = to;
        AgentsMapByApplication allAgentsList = this.agentInfoService.getAllAgentsList(filter, timestamp);
        return treeView(allAgentsList);
    }


    @GetMapping(value = "/getAgentList", params = {"!application", "timestamp"})
    public TreeView<AgentStatusAndLink> getAgentList(
            @RequestParam("timestamp") long timestamp) {
        AgentsMapByApplication allAgentsList = this.agentInfoService.getAllAgentsList(AgentInfoFilter::accept, timestamp);
        return treeView(allAgentsList);
    }

    private static TreeView<AgentStatusAndLink> treeView(AgentsMapByApplication agentsListsList) {
        List<AgentsList<AgentStatusAndLink>> list = agentsListsList.getAgentsListsList();
        return new SimpleTreeView<>(list, AgentsList::getGroupName, AgentsList::getAgentSuppliersList);
    }

    @GetMapping(value = "/getAgentList", params = {"application"})
    public TreeView<AgentAndStatus> getAgentList(@RequestParam("application") String applicationName) {
        long timestamp = System.currentTimeMillis();
        return getAgentList(applicationName, timestamp);
    }

    @GetMapping(value = "/getAgentList", params = {"application", "from", "to"})
    public TreeView<AgentAndStatus> getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        AgentInfoFilter currentRunnedFilter = new AgentInfoFilterChain(
                new DefaultAgentInfoFilter(from)
        );
        long timestamp = to;
        AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(currentRunnedFilter, applicationName, timestamp);
        return treeView(list);
    }

    @GetMapping(value = "/getAgentList", params = {"application", "timestamp"})
    public TreeView<AgentAndStatus> getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("timestamp") long timestamp) {
        AgentInfoFilter runningAgentFilter = new AgentInfoFilterChain(
                AgentInfoFilter::filterRunning
        );
        AgentsMapByHost list = this.agentInfoService.getAgentsListByApplicationName(runningAgentFilter, applicationName, timestamp);
        return treeView(list);
    }

    private static TreeView<AgentAndStatus> treeView(AgentsMapByHost agentsMapByHost) {
        List<AgentsList<AgentAndStatus>> list = agentsMapByHost.getAgentsListsList();
        return new SimpleTreeView<>(list, AgentsList::getGroupName, AgentsList::getAgentSuppliersList);
    }

    @GetMapping(value = "/getAgentInfo")
    public AgentAndStatus getAgentInfo(
            @RequestParam("agentId") String agentId,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getAgentInfo(agentId, timestamp);
    }

    @GetMapping(value = "/getDetailedAgentInfo")
    public DetailedAgentAndStatus getDetailedAgentInfo(
            @RequestParam("agentId") String agentId,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getDetailedAgentInfo(agentId, timestamp);
    }

    @GetMapping(value = "/getAgentStatus")
    public AgentStatus getAgentStatus(
            @RequestParam("agentId") String agentId,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getAgentStatus(agentId, timestamp);
    }

    @GetMapping(value = "/getAgentEvent")
    public AgentEvent getAgentEvent(
            @RequestParam("agentId") String agentId,
            @RequestParam("eventTimestamp") long eventTimestamp,
            @RequestParam("eventTypeCode") int eventTypeCode) {

        final AgentEventType eventType = AgentEventType.getTypeByCode(eventTypeCode);
        if (eventType == null) {
            throw new IllegalArgumentException("invalid eventTypeCode [" + eventTypeCode + "]");
        }

        return this.agentEventService.getAgentEvent(agentId, eventTimestamp, eventType);
    }

    @GetMapping(value = "/getAgentEvents")
    public List<AgentEvent> getAgentEvents(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        Range range = Range.between(from, to);
        Set<AgentEventType> excludeEventTypes = getAgentEventTypes(excludeEventTypeCodes);
        return this.agentEventService.getAgentEvents(agentId, range, excludeEventTypes);
    }

    private Set<AgentEventType> getAgentEventTypes(int[] excludeEventTypeCodes) {
        Set<AgentEventType> excludeEventTypes = EnumSet.noneOf(AgentEventType.class);
        for (int excludeEventTypeCode : excludeEventTypeCodes) {
            AgentEventType excludeEventType = AgentEventType.getTypeByCode(excludeEventTypeCode);
            if (excludeEventType != null) {
                excludeEventTypes.add(excludeEventType);
            }
        }
        return excludeEventTypes;
    }

    @GetMapping(value = "/getAgentStatusTimeline")
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range);
    }

    @GetMapping(value = "/getAgentStatusTimeline", params = {"exclude"})
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        Range range = Range.between(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range, excludeEventTypeCodes);
    }

    @RequestMapping(value = "/isAvailableAgentId")
    public CodeResult isAvailableAgentId(@RequestParam("agentId") String agentId) {
        final IdValidateUtils.CheckResult result = IdValidateUtils.checkId(agentId, PinpointConstants.AGENT_ID_MAX_LEN);
        if (result == IdValidateUtils.CheckResult.FAIL_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "length range is 1 ~ 24");
        }
        if (result == IdValidateUtils.CheckResult.FAIL_PATTERN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pattern(" + IdValidateUtils.ID_PATTERN_VALUE + ")");
        }
        if (agentInfoService.isExistAgentId(agentId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "agentId already exists");
        }
        return CodeResult.ok("OK");
    }

}
