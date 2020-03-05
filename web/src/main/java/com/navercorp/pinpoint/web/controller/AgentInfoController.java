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

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentDownloadInfo;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentInstallationInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.ApplicationAgentsList;
import com.navercorp.pinpoint.web.vo.CodeResult;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Controller
public class AgentInfoController {

    private static final int CODE_SUCCESS = 0;
    private static final int CODE_FAIL = -1;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private AgentEventService agentEventService;

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application"})
    @ResponseBody
    public ApplicationAgentsList getAgentList() {
        long timestamp = System.currentTimeMillis();
        return getAgentList(timestamp);
    }

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application", "from", "to"})
    @ResponseBody
    public ApplicationAgentsList getAgentList(
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        long timestamp = to;
        return this.agentInfoService.getAllApplicationAgentsList(ApplicationAgentsList.Filter.NONE, timestamp);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application", "timestamp"})
    @ResponseBody
    public ApplicationAgentsList getAgentList(
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getAllApplicationAgentsList(ApplicationAgentsList.Filter.NONE, timestamp);
    }

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application"})
    @ResponseBody
    public ApplicationAgentsList getAgentList(@RequestParam("application") String applicationName) {
        long timestamp = System.currentTimeMillis();
        return getAgentList(applicationName, timestamp);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application", "from", "to"})
    @ResponseBody
    public ApplicationAgentsList getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        ApplicationAgentsList.Filter containerFilter = agentInfo -> {
            if (!agentInfo.isContainer()) {
                return ApplicationAgentsList.Filter.ACCEPT;
            }
            AgentStatus agentStatus = agentInfo.getStatus();
            if (agentStatus == null) {
                return ApplicationAgentsList.Filter.REJECT;
            }
            if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
                return ApplicationAgentsList.Filter.ACCEPT;
            }
            if (agentStatus.getEventTimestamp() >= from) {
                return ApplicationAgentsList.Filter.ACCEPT;
            }
            return ApplicationAgentsList.Filter.REJECT;
        };
        long timestamp = to;
        return this.agentInfoService.getApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, containerFilter, applicationName, timestamp);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application", "timestamp"})
    @ResponseBody
    public ApplicationAgentsList getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("timestamp") long timestamp) {
        ApplicationAgentsList.Filter runningContainerFilter = agentInfo -> {
            if (!agentInfo.isContainer()) {
                return ApplicationAgentsList.Filter.ACCEPT;
            }
            AgentStatus agentStatus = agentInfo.getStatus();
            if (agentStatus == null) {
                return ApplicationAgentsList.Filter.REJECT;
            }
            if (agentStatus.getState() == AgentLifeCycleState.RUNNING) {
                return ApplicationAgentsList.Filter.ACCEPT;
            }
            return ApplicationAgentsList.Filter.REJECT;
        };
        return this.agentInfoService.getApplicationAgentsList(ApplicationAgentsList.GroupBy.HOST_NAME, runningContainerFilter, applicationName, timestamp);
    }

    @RequestMapping(value = "/getAgentInfo", method = RequestMethod.GET)
    @ResponseBody
    public AgentInfo getAgentInfo(
            @RequestParam("agentId") String agentId,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getAgentInfo(agentId, timestamp);
    }

    @RequestMapping(value = "/getAgentStatus", method = RequestMethod.GET)
    @ResponseBody
    public AgentStatus getAgentStatus(
            @RequestParam("agentId") String agentId,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getAgentStatus(agentId, timestamp);
    }

    @RequestMapping(value = "/getAgentEvent", method = RequestMethod.GET)
    @ResponseBody
    public AgentEvent getAgentEvent(
            @RequestParam("agentId") String agentId,
            @RequestParam("eventTimestamp") long eventTimestamp,
            @RequestParam("eventTypeCode") int eventTypeCode) {
        return this.agentEventService.getAgentEvent(agentId, eventTimestamp, eventTypeCode);
    }

    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentEvents", method = RequestMethod.GET)
    @ResponseBody
    public List<AgentEvent> getAgentEvents(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        Range range = new Range(from, to);
        return this.agentEventService.getAgentEvents(agentId, range, excludeEventTypeCodes);
    }

    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentStatusTimeline", method = RequestMethod.GET)
    @ResponseBody
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        Range range = new Range(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range);
    }

    @PreAuthorize("hasPermission(new com.navercorp.pinpoint.web.vo.AgentParam(#agentId, #to), 'agentParam', 'inspector')")
    @RequestMapping(value = "/getAgentStatusTimeline", method = RequestMethod.GET, params = {"exclude"})
    @ResponseBody
    public InspectorTimeline getAgentStatusTimeline(
            @RequestParam("agentId") String agentId,
            @RequestParam("from") long from,
            @RequestParam("to") long to,
            @RequestParam(value = "exclude", defaultValue = "") int[] excludeEventTypeCodes) {
        Range range = new Range(from, to);
        return agentInfoService.getAgentStatusTimeline(agentId, range, excludeEventTypeCodes);
    }

    @RequestMapping(value = "/isAvailableAgentId")
    @ResponseBody
    public CodeResult isAvailableAgentId(@RequestParam("agentId") String agentId) {
        if (!IdValidateUtils.checkLength(agentId, PinpointConstants.AGENT_NAME_MAX_LEN)) {
            return new CodeResult(CODE_FAIL, "length range is 1 ~ 24");
        }

        if (!IdValidateUtils.validateId(agentId, PinpointConstants.AGENT_NAME_MAX_LEN)) {
            return new CodeResult(CODE_FAIL, "invalid pattern(" + IdValidateUtils.ID_PATTERN_VALUE + ")");
        }

        if (agentInfoService.isExistAgentId(agentId)) {
            return new CodeResult(CODE_FAIL, "already exist agentId");
        }

        return new CodeResult(CODE_SUCCESS, "OK");
    }

    @RequestMapping(value = "/getAgentInstallationInfo")
    @ResponseBody
    public CodeResult getAgentDownloadUrl() {
        AgentDownloadInfo latestStableAgentDownloadInfo = agentInfoService.getLatestStableAgentDownloadInfo();
        if (latestStableAgentDownloadInfo != null) {
            return new CodeResult(0, new AgentInstallationInfo(latestStableAgentDownloadInfo));
        }

        return new CodeResult(-1, "can't find suitable download url");
    }

}
