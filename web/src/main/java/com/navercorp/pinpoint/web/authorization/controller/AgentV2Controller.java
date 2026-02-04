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
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntryAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@RestController
@RequestMapping(value = "/api")
@Validated
public class AgentV2Controller {

    private final ServiceUidService serviceUidService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentListV2Service agentListV2Service;
    private final AgentsService agentsService;

    public AgentV2Controller(@Autowired(required = false) ServiceUidService serviceUidService,
                             ServiceTypeRegistryService serviceTypeRegistryService,
                             AgentListV2Service agentListV2Service,
                             AgentsService agentsService) {
        this.serviceUidService = serviceUidService;
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
        this.agentsService = Objects.requireNonNull(agentsService, "agentsService");
    }

    @PreAuthorize("hasPermission(#application, 'application', 'inspector')")
    @GetMapping(value = "/agents", params = {"application", "from", "to"})
    public List<AgentIdEntryAndStatus> getAgentsListOldParam(
            @RequestParam("application") @NotBlank String application,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "readV2", required = false) Optional<Boolean> readV2) {
        return getAgentsList(null, application, serviceTypeCode, serviceTypeName, from, to, readV2);
    }

    @PreAuthorize("hasPermission(#applicationName, 'application', 'inspector')")
    @GetMapping(value = "/agents", params = {"applicationName", "from", "to"})
    public List<AgentIdEntryAndStatus> getAgentsList(
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to,
            @RequestParam(value = "readV2", required = false) Optional<Boolean> readV2) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);

        final boolean useV2 = readV2.orElse(true);
        if (useV2) {
            if (serviceType.equals(ServiceType.UNDEFINED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceType. ServiceType is required for v2 table");
            }
            return agentListV2Service.getAgentList(serviceUid, applicationName, serviceType, range);
        } else {
            if (!ServiceUid.DEFAULT.equals(serviceUid)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "v1 table only supports 'default' service");
            }
            return agentsService.getAgentsByApplicationName(new Application(applicationName, serviceType), new TimeWindow(range),
                            ApplicationAgentListQueryRule.ACTIVE_STATUS, AgentInfoFilters.acceptAll()).stream()
                    .map(this::v1ToV2Format)
                    .toList();
        }
    }

    private AgentIdEntryAndStatus v1ToV2Format(AgentStatusAndLink agentStatusAndLink) {
        AgentInfo agentInfo = agentStatusAndLink.getAgentInfo();
        Application application = new Application(agentInfo.getApplicationName(), agentInfo.getServiceType());
        AgentIdEntry agentIdEntry = new AgentIdEntry(application, agentInfo.getAgentId(), agentInfo.getStartTimestamp(),
                0, agentInfo.getAgentName());
        if (agentStatusAndLink.getStatus() == null) {
            return new AgentIdEntryAndStatus(agentIdEntry, new AgentStatus(agentInfo.getAgentId(), AgentLifeCycleState.UNKNOWN, 0));
        } else {
            return new AgentIdEntryAndStatus(agentIdEntry, agentStatusAndLink.getStatus());
        }
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
