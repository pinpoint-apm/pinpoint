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

import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.web.service.AdminService;
import com.navercorp.pinpoint.web.service.ApplicationInfoService;
import com.navercorp.pinpoint.web.service.ServiceInfoService;
import com.navercorp.pinpoint.web.vo.Application;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
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
@RequestMapping("/admin")
@Validated
public class AdminController {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AdminService adminService;
    private final ServiceInfoService serviceInfoService;
    private final ApplicationInfoService applicationInfoService;

    public AdminController(
            AdminService adminService,
            ServiceInfoService serviceInfoService,
            ApplicationInfoService applicationInfoService
    ) {
        this.adminService = Objects.requireNonNull(adminService, "adminService");
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
    }

    @RequestMapping(value = "/removeApplicationName")
    public String removeApplicationName(
            @RequestParam(value = "serviceName", defaultValue = ServiceId.DEFAULT_SERVICE_NAME) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", defaultValue = "5004") short serviceTypeCode
    ) {
        try {
            ApplicationId applicationId = this.getApplicationId(serviceName, applicationName, serviceTypeCode);
            this.adminService.removeApplicationName(applicationId);
            return "OK";
        } catch (Exception e) {
            logger.error("error while removing applicationName", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @RequestMapping(value = "/removeAgentId")
    public String removeAgentId(
            @RequestParam(value = "serviceName", defaultValue = ServiceId.DEFAULT_SERVICE_NAME) String serviceName,
            @RequestParam(value = "applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", defaultValue = "5004") short serviceTypeCode,
            @RequestParam(value = "agentId") @NotBlank String agentId
    ) {
        logger.info("Removing agent - applicationName: [{}], agentId: [{}]", applicationName, agentId);
        try {
            ApplicationId applicationId = this.getApplicationId(serviceName, applicationName, serviceTypeCode);
            this.adminService.removeAgentId(applicationId, agentId);
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

    @RequestMapping(value = "/agentIdMap")
    public Map<String, List<Application>> agentIdMap() {
        return this.adminService.getAgentIdMap();
    }

    @RequestMapping(value = "/duplicateAgentIdMap")
    public Map<String, List<Application>> duplicateAgentIdMap() {
        return this.adminService.getDuplicateAgentIdMap();
    }

    @RequestMapping(value = "/getInactiveAgents")
    public Map<String, List<Application>> getInactiveAgents(
            @RequestParam(value = "serviceName", defaultValue = ServiceId.DEFAULT_SERVICE_NAME) String serviceName,
            @RequestParam(value = "applicationName") @NotBlank String applicationName,
            @RequestParam(value = "serviceTypeCode", defaultValue = "5004") short serviceTypeCode,
            @RequestParam(value = "durationDays", defaultValue = AdminService.MIN_DURATION_DAYS_FOR_INACTIVITY_STR)
            @Min(AdminService.MIN_DURATION_DAYS_FOR_INACTIVITY)
            int durationDays
    ) {
        logger.info("Getting in-active agents - applicationName: [{}], duration: {} days.",
                applicationName, durationDays);
        ApplicationId applicationId = this.getApplicationId(serviceName, applicationName, serviceTypeCode);
        return this.adminService.getInactiveAgents(applicationId, durationDays);
    }

    private ApplicationId getApplicationId(String serviceName, String applicationName, short serviceTypeCode) {
        ServiceId serviceId = this.serviceInfoService.getServiceId(serviceName);
        if (serviceId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Service [" + serviceName + "] not found");
        }

        ApplicationId applicationId = this.applicationInfoService.getApplicationId(new ApplicationSelector(serviceId, applicationName, serviceTypeCode));
        if (applicationId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application [" + applicationName + "] not found");
        }

        return applicationId;
    }

}