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
import com.navercorp.pinpoint.web.response.CodeResult;
import com.navercorp.pinpoint.web.service.AgentInfoService;
import com.navercorp.pinpoint.web.service.ApplicationIndexService;
import com.navercorp.pinpoint.web.service.CacheService;
import com.navercorp.pinpoint.web.service.CommonService;
import com.navercorp.pinpoint.web.util.TagApplicationsUtils;
import com.navercorp.pinpoint.web.view.TagApplications;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilters;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */

@RestController
@RequestMapping("/api")
@Validated
public class ApplicationController {
    public static final int MAX_PAGING_LIMIT = 100;
    public static final int MAX_DURATION_DAYS = 7;

    private final AgentInfoService agentInfoService;

    private final ApplicationIndexService applicationIndexService;

    private final CommonService commonService;
    private final CacheService cacheService;

    private static final String KEY = CacheService.DEFAULT_KEY;

    public ApplicationController(AgentInfoService agentInfoService, ApplicationIndexService applicationIndexService, CommonService commonService, CacheService cacheService) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationIndexService = Objects.requireNonNull(applicationIndexService, "applicationIndexService");
        this.commonService = Objects.requireNonNull(commonService, "commonService");
        this.cacheService = Objects.requireNonNull(cacheService, "cacheService");
    }

    @GetMapping(value = "/getApplicationHostInfo", params = "durationHours")
    public ApplicationAgentHostList getApplicationHostInfoV2(
            @RequestParam(value = "offset", required = false, defaultValue = "1") @Positive int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") @Positive int limit,
            @RequestParam(value = "durationHours", required = false, defaultValue = "0") @PositiveOrZero int durationHours,
            @RequestParam(value = "useCache", required = false, defaultValue = "true") Boolean useCache,
            @RequestParam(value = "isContainer", required = false) Boolean isContainer
    ) {
        int maxLimit = Math.min(MAX_PAGING_LIMIT, limit);
        durationHours = Math.min(MAX_DURATION_DAYS * 24, durationHours);
        AgentInfoFilter agentInfoFilter = createAgentInfoFilter(isContainer);

        List<Application> applicationList = getApplicationList(useCache);

        return agentInfoService.getApplicationAgentHostList(offset, maxLimit, durationHours, applicationList, agentInfoFilter);
    }

    @GetMapping(value = "/getApplicationHostInfo")
    public ApplicationAgentHostList getApplicationHostInfoDaysV2(
            @RequestParam(value = "offset", required = false, defaultValue = "1") @Positive int offset,
            @RequestParam(value = "limit", required = false, defaultValue = "100") @Positive int limit,
            @RequestParam(value = "durationDays", required = false, defaultValue = "0") @PositiveOrZero int durationDays,
            @RequestParam(value = "useCache", required = false, defaultValue = "true") Boolean useCache,
            @RequestParam(value = "isContainer", required = false) Boolean isContainer
    ) {
        int maxLimit = Math.min(MAX_PAGING_LIMIT, limit);
        int durationHours = Math.min(MAX_DURATION_DAYS * 24, durationDays * 24);
        return getApplicationHostInfoV2(offset, maxLimit, durationHours, useCache, isContainer);
    }

    private AgentInfoFilter createAgentInfoFilter(Boolean isContainer) {
        if (isContainer != null) {
            return AgentInfoFilters.isContainer(isContainer);
        }
        return AgentInfoFilters.acceptAll();
    }

    private List<Application> getApplicationList(boolean useCache) {
        if (!useCache) {
            return commonService.selectAllApplicationNames();
        }

        final TagApplications cachedTagApplications = cacheService.get(KEY);
        if (cachedTagApplications != null) {
            return cachedTagApplications.getApplicationList();
        }
        final List<Application> applicationList = commonService.selectAllApplicationNames();
        final TagApplications tagApplications = TagApplicationsUtils.wrapApplicationList(applicationList);
        cacheService.put(KEY, tagApplications);
        return applicationList;
    }

    @RequestMapping(value = "/isAvailableApplicationName")
    public CodeResult<String> isAvailableApplicationName(
            @RequestParam("applicationName") @NotBlank String applicationName
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

        if (applicationIndexService.isExistApplicationName(applicationName)) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "applicationName already exists");
        }

        return CodeResult.ok("OK");
    }

}