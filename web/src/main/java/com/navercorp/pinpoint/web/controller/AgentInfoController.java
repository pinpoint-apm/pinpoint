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

import com.navercorp.pinpoint.web.service.AgentEventService;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
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

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private AgentEventService agentEventService;

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application"})
    @ResponseBody
    public ApplicationAgentList getAgentList() {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.APPLICATION_NAME);
    }

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application", "from", "to"})
    @ResponseBody
    public ApplicationAgentList getAgentList(
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.APPLICATION_NAME, to);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"!application", "timestamp"})
    @ResponseBody
    public ApplicationAgentList getAgentList(
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.APPLICATION_NAME, timestamp);
    }

    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application"})
    @ResponseBody
    public ApplicationAgentList getAgentList(
            @RequestParam("application") String applicationName) {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.HOST_NAME, applicationName);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application", "from", "to"})
    @ResponseBody
    public ApplicationAgentList getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("from") long from,
            @RequestParam("to") long to) {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.HOST_NAME, applicationName, to);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @RequestMapping(value = "/getAgentList", method = RequestMethod.GET, params = {"application", "timestamp"})
    @ResponseBody
    public ApplicationAgentList getAgentList(
            @RequestParam("application") String applicationName,
            @RequestParam("timestamp") long timestamp) {
        return this.agentInfoService.getApplicationAgentList(ApplicationAgentList.Key.HOST_NAME, applicationName, timestamp);
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
        return agentInfoService.getAgentStatusTimeline(agentId, range, null);
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
}
