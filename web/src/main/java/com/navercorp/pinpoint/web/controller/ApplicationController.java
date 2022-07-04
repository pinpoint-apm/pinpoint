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
import com.navercorp.pinpoint.common.util.IdValidateUtils;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationService;
import com.navercorp.pinpoint.web.vo.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.response.CodeResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * @author Taejin Koo
 */

@RestController
public class ApplicationController {

    private final AgentInfoService agentInfoService;

    private final ApplicationService applicationService;

    public ApplicationController(AgentInfoService agentInfoService, ApplicationService applicationService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
    }

    @GetMapping(value = "/getApplicationHostInfo", params = {"!durationDays"})
    public ApplicationAgentHostList getApplicationHostInfo (
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        return agentInfoService.getApplicationAgentHostList(offset, limit);
    }

    @GetMapping(value = "/getApplicationHostInfo", params = {"durationDays"})
    public ApplicationAgentHostList getApplicationHostInfo (
            @RequestParam(value = "offset", required = false, defaultValue = "1") int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit,
            @RequestParam(value = "durationDays") int durationDays) {
        return agentInfoService.getApplicationAgentHostList(offset, limit, durationDays);
    }

    @RequestMapping(value = "/isAvailableApplicationName")
    public CodeResult isAvailableApplicationName(@RequestParam("applicationName") String applicationName) {
        final IdValidateUtils.CheckResult result = IdValidateUtils.checkId(applicationName, PinpointConstants.APPLICATION_NAME_MAX_LEN);
        if (result == IdValidateUtils.CheckResult.FAIL_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "length range is 1 ~ 24");
        }
        if (result == IdValidateUtils.CheckResult.FAIL_PATTERN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid pattern(" + IdValidateUtils.ID_PATTERN_VALUE + ")");
        }

        if (applicationService.isExistApplicationName(applicationName)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "applicationName already exists");
        }

        return CodeResult.ok("OK");
    }

}