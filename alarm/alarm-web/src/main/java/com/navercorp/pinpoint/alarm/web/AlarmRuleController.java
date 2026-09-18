/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.web;

import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import com.navercorp.pinpoint.alarm.vo.AlarmState;
import com.navercorp.pinpoint.service.web.vo.ServiceConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/alarm/rule")
@Validated
public class AlarmRuleController {

    private final AlarmRuleService alarmRuleService;

    public AlarmRuleController(AlarmRuleService alarmRuleService) {
        this.alarmRuleService = Objects.requireNonNull(alarmRuleService, "alarmRuleService");
    }

    /**
     * Fills in the serviceName of the {@link AlarmApplication} the read endpoints bind,
     * so that reads take it from the same place the writes do -- the header.
     * <p>
     * A {@code ?serviceName=} parameter binds onto the attribute and wins over this
     * value. That is left alone on purpose: reads are open across services, and the web
     * app ships as one version, so no client sends it by accident.
     */
    @ModelAttribute
    AlarmApplication alarmApplication(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName) {
        AlarmApplication application = new AlarmApplication();
        application.setServiceName(serviceName);
        return application;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #rule.applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public AlarmRuleV2 createRule(
            @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
            @NotBlank
            @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
            String serviceName,
            @RequestBody @Valid AlarmRuleV2 rule) {
        rule.setServiceName(serviceName);
        return alarmRuleService.createRule(rule);
    }

    @GetMapping("/{id}")
    public AlarmRuleResponse getRule(@PathVariable Long id,
                                     @Valid @ModelAttribute AlarmApplication application) {
        return alarmRuleService.getRuleResponse(id, application);
    }

    @GetMapping
    public List<AlarmRuleResponse> getRules(
            @Valid @ModelAttribute AlarmApplication application) {
        return alarmRuleService.getRuleResponsesByApplication(application);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #rule.applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void updateRule(@PathVariable Long id,
                           @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                           @NotBlank
                           @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                           String serviceName,
                           @RequestBody @Valid AlarmRuleV2 rule) {
        rule.setServiceName(serviceName);
        alarmRuleService.updateRule(id, rule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void deleteRule(@PathVariable Long id,
                           @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                           @NotBlank
                           @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                           String serviceName,
                           @RequestParam("applicationName") @NotBlank String applicationName) {
        alarmRuleService.deleteRule(serviceName, applicationName, id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("@naverPermissionEvaluator.hasAlarmPermission(#serviceName, #applicationName, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    public void updateEnabled(@PathVariable Long id,
                              @RequestHeader(value = ServiceConstants.KEY, defaultValue = ServiceConstants.DEFAULT)
                              @NotBlank
                              @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
                              String serviceName,
                              @RequestParam("applicationName") @NotBlank String applicationName,
                              @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            throw new IllegalArgumentException("'enabled' field is required");
        }
        alarmRuleService.updateEnabled(serviceName, applicationName, id, enabled);
    }

    @GetMapping("/{id}/state")
    public AlarmState getRuleState(@PathVariable Long id,
                                   @Valid @ModelAttribute AlarmApplication application) {
        AlarmState state = alarmRuleService.getRuleState(id, application);
        if (state == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "State not found for rule: " + id);
        }
        return state;
    }

    @GetMapping("/{id}/history")
    public List<AlarmHistoryV2> getRuleHistory(
            @PathVariable Long id,
            @Valid @ModelAttribute AlarmApplication application,
            @RequestParam(defaultValue = "50") int limit) {
        return alarmRuleService.getRuleHistory(id, application, Math.min(Math.max(limit, 1), 1000));
    }
}
