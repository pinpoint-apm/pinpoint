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

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.response.AlarmResponse;
import com.navercorp.pinpoint.web.service.AlarmService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping(value={"/api/alarmRule", "/api/application/alarmRule"})
@Validated
public class AlarmController {

    public final static String USER_GROUP_ID_PARAMS = "userGroupId";
    public final static String APPLICATION_ID_PARAMS = "applicationId";

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
    }

    @PreAuthorize("hasPermission(#rule.getApplicationName(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    @PostMapping
    public AlarmResponse insertRule(@RequestBody Rule rule) {
        if (Rule.isRuleInvalidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        final String ruleId = alarmService.insertRule(rule);
        return new AlarmResponse(Result.SUCCESS, ruleId);
    }

    @PreAuthorize("hasPermission(#rule.getApplicationName(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    @DeleteMapping
    public Response deleteRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId to delete alarm rule");
        }
        alarmService.deleteRule(rule);
        return SimpleResponse.ok();
    }

    @GetMapping(params = USER_GROUP_ID_PARAMS)
    public List<Rule> getRulesByUserGroup(@RequestParam(value = USER_GROUP_ID_PARAMS) @NotBlank String userGroupId) {
        return alarmService.selectRuleByUserGroupId(userGroupId);
    }

    @GetMapping(params = APPLICATION_ID_PARAMS)
    public List<Rule> getRulesByApplication(@RequestParam(value = APPLICATION_ID_PARAMS) @NotBlank String applicationName) {
        return alarmService.selectRuleByApplicationName(applicationName);
    }

    @PreAuthorize("hasPermission(#rule.getApplicationName(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    @PutMapping
    public Response updateRule(@RequestBody Rule rule) {
        if (Rule.isRuleInvalid(rule)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is not ruleId/userGroupId/applicationId/checkerName to publish alarm rule"
            );
        }
        alarmService.updateRule(rule);
        return SimpleResponse.ok();
    }

    @GetMapping(value = "/checker")
    public List<String> getCheckerName() {
        return CheckerCategory.getNames();
    }

}
