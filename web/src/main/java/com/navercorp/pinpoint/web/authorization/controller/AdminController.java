/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.service.web.resolver.ServiceParam;
import com.navercorp.pinpoint.service.web.vo.ServiceName;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.service.AdminService;
import com.navercorp.pinpoint.web.service.AgentListV2Service;
import com.navercorp.pinpoint.web.service.ServiceModelResolver;
import com.navercorp.pinpoint.web.view.tree.AgentIdView;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Service;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@RestController
@PreAuthorize("hasPermission(null, null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ADMINISTRATION_CALL_API_FOR_APP_AGENT_MANAGEMENT)")
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AdminService adminService;
    private final ApplicationFactory applicationFactory;
    private final ServiceModelResolver serviceModelResolver;
    private final AgentListV2Service agentListV2Service;

    public AdminController(AdminService adminService,
                           ApplicationFactory applicationFactory,
                           ServiceModelResolver serviceModelResolver,
                           AgentListV2Service agentListV2Service) {
        this.adminService = Objects.requireNonNull(adminService, "adminService");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serviceModelResolver = Objects.requireNonNull(serviceModelResolver, "serviceModelResolver");
        this.agentListV2Service = Objects.requireNonNull(agentListV2Service, "agentListV2Service");
    }

    @Deprecated
    @RequestMapping(value = "/removeApplicationName")
    public String removeApplicationName(@RequestParam("applicationName") @NotBlank String applicationName) {
        logger.info("Removing application - applicationName: [{}]", applicationName);
        try {
            this.adminService.removeApplicationName(applicationName);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing applicationName", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping(value = "/applications")
    public String removeApplication(@ServiceParam ServiceName serviceName,
                                    @RequestParam("applicationName") @NotBlank String applicationName,
                                    @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
                                    @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName) {
        Service service = serviceModelResolver.getService(serviceName.getName());
        Application application = getApplication(service, applicationName, serviceTypeCode, serviceTypeName);
        if (application.getServiceType().equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Undefined service type");
        }
        logger.info("Removing application - application: {}", application);
        try {
            this.adminService.removeApplication(application.getService(), application.getApplicationName(), application.getServiceTypeCode());
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing application", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Deprecated
    @RequestMapping(value = "/removeAgentId", params = {"!serviceTypeCode", "!serviceTypeName"})
    public String removeAgentId(
            @RequestParam(value = "applicationName") @NotBlank String applicationName,
            @RequestParam(value = "agentId") @NotBlank String agentId
    ) {
        logger.info("Removing agent - applicationName: [{}], agentId: [{}]", applicationName, agentId);
        try {
            this.adminService.removeAgentId(applicationName, agentId);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing agentId", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @DeleteMapping(value = "/agents")
    public String removeAgent(
            @ServiceParam ServiceName serviceName,
            @RequestParam(value = "applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName,
            @RequestParam(value = "agentId") @NotBlank String agentId) {
        Service service = serviceModelResolver.getService(serviceName.getName());
        Application application = getApplication(service, applicationName, serviceTypeCode, serviceTypeName);
        if (application.getServiceType().equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Undefined service type");
        }
        logger.info("Removing agent - application: {}, agentId: {}", application, agentId);
        try {
            this.adminService.removeAgent(application.getService(), application.getApplicationName(), application.getServiceTypeCode(), agentId);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing agentId", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/removeInactiveAgents")
    @Deprecated
    public String removeInactiveAgents(
            @RequestParam(value = "durationDays", defaultValue = "30") @PositiveOrZero int durationDays
    ) {
        logger.info("Removing agents which have been inactive for the last {} days", durationDays);
        try {
            this.adminService.removeInactiveAgents(durationDays);
            return "OK";
        } catch (Exception e) {
            logger.error("Error occurred while removing agents which have been inactive for the last {} days",
                    durationDays, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Deprecated
    @RequestMapping(value = "/agentIdMap")
    public Map<String, List<Application>> agentIdMap() {
        return this.adminService.getAgentIdMap();
    }

    @Deprecated
    @RequestMapping(value = "/duplicateAgentIdMap")
    public Map<String, List<Application>> duplicateAgentIdMap() {
        return this.adminService.getDuplicateAgentIdMap();
    }

    @Deprecated
    @RequestMapping(value = "/getInactiveAgents")
    public Map<String, List<Application>> getInactiveAgents(
            @RequestParam(value = "applicationName") @NotBlank String applicationName,
            @RequestParam(value = "durationDays", defaultValue = AdminService.MIN_DURATION_DAYS_FOR_INACTIVITY_STR)
            @Min(AdminService.MIN_DURATION_DAYS_FOR_INACTIVITY)
            int durationDays
    ) {
        logger.info("Getting in-active agents - applicationName: [{}], duration: {} days.",
                applicationName, durationDays);
        return this.adminService.getInactiveAgents(applicationName, durationDays);
    }

    @GetMapping(value = "/agents")
    public List<AgentIdView> getAgents(
            @ServiceParam ServiceName serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", required = false) Integer serviceTypeCode,
            @RequestParam(value = "serviceTypeName", required = false) String serviceTypeName) {
        final Service service = serviceModelResolver.getService(serviceName.getName());
        final Application application = getApplication(service, applicationName, serviceTypeCode, serviceTypeName);
        if (application.getServiceType().equals(ServiceType.UNDEFINED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid serviceType. ServiceType is required");
        }
        return agentListV2Service.getAllAgentList(service, applicationName, application.getServiceType()).stream()
                .map(AgentIdView::of)
                .toList();
    }

    private Application getApplication(Service service, String applicationName, Integer serviceTypeCode, String serviceTypeName) {
        if (serviceTypeCode != null) {
            return applicationFactory.createApplication(service, applicationName, serviceTypeCode);
        } else if (serviceTypeName != null) {
            return applicationFactory.createApplicationByTypeName(service, applicationName, serviceTypeName);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No service type provided.");
    }
}
