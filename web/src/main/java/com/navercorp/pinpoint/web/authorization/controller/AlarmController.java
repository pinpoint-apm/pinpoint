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
import com.navercorp.pinpoint.common.server.response.SuccessResponse;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.response.AlarmResponse;
import com.navercorp.pinpoint.web.service.AlarmService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
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
@RequestMapping(value={"/alarmRule", "/application/alarmRule"})
@Validated
public class AlarmController {

    public final static String USER_GROUP_ID = "userGroupId";
    public final static String APPLICATION_ID = "applicationId";

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
    }

    @PostMapping
    public AlarmResponse insertRule(@RequestBody Rule rule) {
        if (Rule.isRuleInvalidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        final String ruleId = alarmService.insertRule(rule);
        return new AlarmResponse("SUCCESS", ruleId);
    }

    @DeleteMapping
    public Response deleteRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId to delete alarm rule");
        }
        alarmService.deleteRule(rule);
        return SuccessResponse.ok();
    }

    @GetMapping(params = USER_GROUP_ID)
    public List<Rule> getRulesByUserGroup(@RequestParam(value = USER_GROUP_ID) @NotBlank String userGroupId) {
        return alarmService.selectRuleByUserGroupId(userGroupId);
    }

    @GetMapping(params = APPLICATION_ID)
    public List<Rule> getRulesByApplication(@RequestParam(value = APPLICATION_ID) @NotBlank String applicationId) {
        return alarmService.selectRuleByApplicationId(applicationId);
    }

    @PutMapping
    public Response updateRule(@RequestBody Rule rule) {
        if (Rule.isRuleInvalid(rule)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is not ruleId/userGroupId/applicationId/checkerName to publish alarm rule"
            );
        }
        alarmService.updateRule(rule);
        return SuccessResponse.ok();
    }

    @GetMapping(value = "/checker")
    public List<String> getCheckerName() {
        return CheckerCategory.getNames();
    }

}
