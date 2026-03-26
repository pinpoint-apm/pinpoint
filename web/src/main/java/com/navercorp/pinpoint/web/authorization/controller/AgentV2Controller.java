/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.agentlist.service.AgentsService;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.service.ApplicationAgentListQueryRule;
import com.navercorp.pinpoint.web.uid.service.ServiceUidService;
import com.navercorp.pinpoint.web.view.tree.AgentIdView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api")
@Validated
public class AgentV2Controller {

    private final ServiceUidService serviceUidService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentListV2Service agentListV2Service;
    private final AgentsService agentsService;
    private final boolean agentReadV2;

    public AgentV2Controller(@Autowired(required = false) ServiceUidService serviceUidService,
                             ServiceTypeRegistryService serviceTypeRegistryService,
                             AgentListV2Service agentListV2Service,
                             AgentsService agentsService,
                             @Value("${pinpoint.web.agent.read.v2:false}") boolean readAgentV2) {
        this.serviceUidService = serviceUidService;
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
        this.agentsService = Objects.requireNonNull(agentsService, "agentsService");
        this.agentReadV2 = readAgentV2;
    }

    @GetMapping("/agents")
    public void forwardAgentsRequest(ServletRequest req, HttpServletResponse res) throws Exception {
        if (agentReadV2) {
            req.getRequestDispatcher("/api/v2/agents").forward(req, res);
            return;
        }
        req.getRequestDispatcher("/api/v1/agents").forward(req, res);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/v2/agents", params = {"applicationName", "from", "to"})
    public List<AgentIdView> getAgentsV2(
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);

        if (serviceType.equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceType. ServiceType is required for v2 table");
        }
        return agentListV2Service.getActiveAgentList(serviceUid, applicationName, serviceType, range).stream()
                .map(entry -> AgentIdView.of(entry, range.getTo()))
                .toList();
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/v1/agents", params = {"applicationName", "from", "to"})
    public List<AgentIdView> getAgentsV1(
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);

        if (!ServiceUid.DEFAULT.equals(serviceUid)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "v1 table only supports 'default' service");
        }
        // Note: AgentsService internally uses v2 agentIds when 'pinpoint.web.agent.read.v2' is true
        return agentsService.getAgentsByApplicationName(new Application(applicationName, serviceType), new TimeWindow(range),
                        ApplicationAgentListQueryRule.ACTIVE_STATUS, AgentInfoFilters.acceptAll()).stream()
                .map(agentStatusAndLink -> v1ToV2Format(agentStatusAndLink, range))
                .toList();
    }

    private AgentIdView v1ToV2Format(AgentStatusAndLink agentStatusAndLink, Range range) {
        AgentInfo agentInfo = agentStatusAndLink.getAgentInfo();
        AgentStatus status = agentStatusAndLink.getStatus();
        Application application = new Application(agentInfo.getApplicationName(), agentInfo.getServiceType());
        return AgentIdView.of(application, agentInfo.getAgentId(), agentInfo.getStartTimestamp(), agentInfo.getAgentName(),
                status != null ? status.getState() : AgentLifeCycleState.UNKNOWN,
                status != null ? status.getEventTimestamp() : 0,
                range.getTo()
        );
    }

    private ServiceUid handleServiceUid(String serviceName) {
        if (serviceUidService == null || com.navercorp.pinpoint.common.util.StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        ServiceUid serviceUid = serviceUidService.getServiceUid(serviceName);
        if (serviceUid == null) {
            throw new IllegalArgumentException("service not found. name: " + serviceName);
        }
        return serviceUid;
    }

    private ServiceType findServiceType(Short serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            return serviceTypeRegistryService.findServiceType(serviceTypeCode);
        } else if (serviceTypeName != null) {
            return serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        }
        return ServiceType.UNDEFINED;
    }
}
