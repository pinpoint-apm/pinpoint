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
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.uid.service.ServiceUidService;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/agents/v2")
@Validated
@ConditionalOnProperty(name = "pinpoint.web.application.index.v2.enabled", havingValue = "true")
public class AgentListV2Controller {

    private final ServiceUidService serviceUidService;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final AgentListV2Service agentListV2Service;

    public AgentListV2Controller(@Autowired(required = false) ServiceUidService serviceUidService,
                                 ServiceTypeRegistryService serviceTypeRegistryService,
                                 AgentListV2Service agentListV2Service) {
        this.serviceUidService = serviceUidService;
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
    }

    @GetMapping(value = "", params = {"application", "from", "to"})
    public List<AgentListItem> getAgentsList(
            @RequestParam("application") @NotBlank String application,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        return getAgentsList(null, application, serviceTypeCode, serviceTypeName, from, to);
    }

    @GetMapping(value = "", params = {"applicationName", "from", "to"})
    public List<AgentListItem> getAgentsList(
            @RequestParam(value = "serviceName", required = false) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Short serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam("from") @PositiveOrZero long from,
            @RequestParam("to") @PositiveOrZero long to) {
        ServiceUid serviceUid = handleServiceUid(serviceName);
        final ServiceType serviceType = findServiceType(applicationName, serviceTypeCode, serviceTypeName);
        Range range = Range.between(from, to);

        if (serviceType.equals(ServiceType.UNDEFINED)) {
            return agentListV2Service.getAgentList(serviceUid, applicationName, range);
        }
        return agentListV2Service.getAgentList(serviceUid, applicationName, serviceType, range);
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

    private ServiceType findServiceType(String applicationName, Short serviceTypeCode, String serviceTypeName) {
        if (StringUtils.hasLength(applicationName)) {
            if (serviceTypeCode != null) {
                return serviceTypeRegistryService.findServiceType(serviceTypeCode);
            } else if (serviceTypeName != null) {
                return serviceTypeRegistryService.findServiceTypeByName(serviceTypeName);
            }
        }
        return ServiceType.UNDEFINED;
    }
}
