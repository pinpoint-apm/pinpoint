/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationInfoService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.service.ServiceInfoService;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Period;
import java.util.Objects;

/**
 * @author Taejin Koo
 */

@RestController
@Validated
public class ApplicationController {
    public static final int MAX_PAGING_LIMIT = 100;

    private final ServiceInfoService serviceInfoService;
    private final ApplicationInfoService applicationInfoService;
    private final AgentInfoService agentInfoService;
    private final ApplicationService applicationService;

    public ApplicationController(
            ServiceInfoService serviceInfoService,
            ApplicationInfoService applicationInfoService,
            AgentInfoService agentInfoService,
            ApplicationService applicationService
    ) {
        this.serviceInfoService = Objects.requireNonNull(serviceInfoService, "serviceInfoService");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
    }

    @GetMapping(value = "/getApplicationHostInfo")
    public ApplicationAgentHostList getApplicationHostInfo (
            @RequestParam(value = "offset", required = false, defaultValue = "1") @Positive int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") @Positive int limit,
            @RequestParam(value = "durationDays", required = false) @PositiveOrZero Integer durationDays
    ) {
        int maxLimit = Math.min(MAX_PAGING_LIMIT, limit);
        durationDays = ObjectUtils.defaultIfNull(durationDays, AgentInfoService.NO_DURATION);

        Period durationDaysPeriod = Period.ofDays(durationDays);
        return agentInfoService.getApplicationAgentHostList(offset, maxLimit, durationDaysPeriod);
    }

    @RequestMapping(value = "/isAvailableApplicationName")
    public CodeResult<String> isAvailableApplicationName(
            @RequestParam(value = "serviceName", defaultValue = ServiceId.DEFAULT_SERVICE_NAME) String serviceName,
            @RequestParam("applicationName") @NotBlank String applicationName,
            @RequestParam("serviceTypeCode") short serviceTypeCode
    ) {
        final IdValidateUtils.CheckResult result =
                IdValidateUtils.checkId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN);
        if (result == IdValidateUtils.CheckResult.FAIL_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "length range is 1 ~ 24");
        }
        if (result == IdValidateUtils.CheckResult.FAIL_PATTERN) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "invalid pattern(" + IdValidateUtils.ID_PATTERN_VALUE + ")"
            );
        }

        ServiceId serviceId = this.serviceInfoService.getServiceId(serviceName);
        if (serviceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serviceName not found");
        }

        ApplicationId applicationId = this.applicationInfoService.getApplicationId(new ApplicationSelector(serviceId, applicationName, serviceTypeCode));


        if (applicationService.isExistApplicationName(applicationId)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "applicationName already exists");
        }

        return CodeResult.ok("OK");
    }

}