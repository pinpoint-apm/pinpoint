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

import com.navercorp.pinpoint.common.timeseries.time.ForwardRangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.time.RangeValidator;
import com.navercorp.pinpoint.common.timeseries.time.Timestamp;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.service.web.resolver.ServiceParam;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.config.ConfigProperties;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.view.tree.AgentIdView;
import com.navercorp.pinpoint.web.vo.Service;
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
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api")
@Validated
public class AgentV2Controller {

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentListV2Service agentListV2Service;
    private final ServiceModelResolver serviceModelResolver;
    private final RangeValidator rangeValidator;

    public AgentV2Controller(ServiceTypeRegistryService serviceTypeRegistryService,
                             AgentListV2Service agentListV2Service,
                             ServiceModelResolver serviceModelResolver,
                             ConfigProperties configProperties) {
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
        this.serviceModelResolver = Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
        this.rangeValidator = new ForwardRangeValidator(Duration.ofDays(configProperties.getInspectorPeriodMax()));
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/agents", params = {"applicationName", "from", "to"})
    public List<AgentIdView> getAgents(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") Timestamp from,
            @RequestParam("to") Timestamp to) {
        final Service service = serviceModelResolver.getService(serviceName.getName());
        final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);
        rangeValidator.validate(range);

        if (serviceType.equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceType. ServiceType is required");
        }
        return agentListV2Service.getActiveAgentList(service, applicationName, serviceType, range).stream()
                .map(entry -> AgentIdView.of(entry, range.getTo()))
                .toList();
    }

    @PreAuthorize("@naverPermissionEvaluator.hasInspectorPermission(#serviceName.getName(), #applicationName)")
    @GetMapping(value = "/agents", params = {"applicationName", "!from", "!to"})
    public List<AgentIdView> getAgents(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName) {
        final Service service = serviceModelResolver.getService(serviceName.getName());
        final ServiceType serviceType = findServiceType(serviceTypeCode, serviceTypeName);
        if (serviceType.equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceType. ServiceType is required");
        }
        return agentListV2Service.getAllAgentList(service, applicationName, serviceType).stream()
                .map(AgentIdView::of)
                .toList();
    }

    private ServiceType findServiceType(Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            return serviceTypeRegistryService.findServiceType(serviceTypeCode);
        } else if (serviceTypeName != null) {
            return serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
        }
        return ServiceType.UNDEFINED;
    }
}
