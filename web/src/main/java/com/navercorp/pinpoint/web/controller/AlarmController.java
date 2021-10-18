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

package com.navercorp.pinpoint.web.controller;

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.WebhookSendInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping
public class AlarmController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public final static String EDIT_ALARM_ONLY_MANAGER = "permission_alarm_editAlarmOnlyManager";
    public final static String USER_GROUP_ID = "userGroupId";
    public final static String APPLICATION_ID = "applicationId";

    private final AlarmService alarmService;
    private final WebhookSendInfoService webhookSendInfoService;

    @Value("${webhook.enable:false}")
    private boolean webhookEnable;

    public AlarmController(AlarmService alarmService, WebhookSendInfoService webhookSendInfoService) {
        this.alarmService = Objects.requireNonNull(alarmService, "alarmService");
        this.webhookSendInfoService = Objects.requireNonNull(webhookSendInfoService, "webhookSendInfoService");
    }

    private Map<String, String> getErrorStringMap(String errorCode, String errorMessage) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("errorCode", errorCode);
        returnMap.put("errorMessage", errorMessage);
        return returnMap;
    }

    private Map<String, String> getResultStringMap(String result, String ruleId) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("result", result);
        returnMap.put("ruleId", ruleId);
        return returnMap;
    }

    private Map<String, String> getResultStringMap(String result) {
        Map<String, String> returnMap = new HashMap<>();
        returnMap.put("result", result);
        return returnMap;
    }

    private boolean isRuleDataValidForPost(Rule rule) {
        if (StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId()) || Objects.isNull(rule.getThreshold())) {
            return false;
        }
        return true;
    }

    @PreAuthorize("hasPermission(#rule.getApplicationId(), null, T(com.navercorp.pinpoint.web.controller.AlarmController).EDIT_ALARM_ONLY_MANAGER)")
    @PostMapping(value={"/alarmRule", "/application/alarmRule"})
    public Map<String, String> insertRule(@RequestBody Rule rule) {
        if (!isRuleDataValidForPost(rule)) {
            return getErrorStringMap("500", "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        String ruleId = alarmService.insertRule(rule);
        return getResultStringMap("SUCCESS", ruleId);
    }

    @PreAuthorize("hasPermission(#rule.getApplicationId(), null, T(com.navercorp.pinpoint.web.controller.AlarmController).EDIT_ALARM_ONLY_MANAGER)")
    @PostMapping(value={"/alarmRuleWithWebhooks", "/application/alarmRuleWithWebhooks"})
    public Map<String, String> insertRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (!isRuleDataValidForPost(rule)) {
            return getErrorStringMap("500", "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }

        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook should be enabled to bind webhook to an alarm");
        }

        String ruleId = alarmService.insertRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());

        return getResultStringMap("SUCCESS", ruleId);
    }

    @PreAuthorize("hasPermission(#rule.getApplicationId(), null, T(com.navercorp.pinpoint.web.controller.AlarmController).EDIT_ALARM_ONLY_MANAGER)")
    @DeleteMapping(value={"/alarmRule", "/application/alarmRule"})
    public Map<String, String> deleteRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId())) {
            return getErrorStringMap("500", "there is not ruleId to delete alarm rule");
        }
        alarmService.deleteRule(rule);
        return getResultStringMap("SUCCESS");
    }
    
    @GetMapping(value={"/alarmRule", "/application/alarmRule"})
    public Object getRule(@RequestParam(value=USER_GROUP_ID, required=false) String userGroupId, @RequestParam(value=APPLICATION_ID, required=false) String applicationId) {
        if (StringUtils.isEmpty(userGroupId) && StringUtils.isEmpty(applicationId)) {
            return getErrorStringMap("500", "there is not userGroupId or applicationID to get alarm rule");
        }
        
        if (StringUtils.hasLength(userGroupId)) {
            return alarmService.selectRuleByUserGroupId(userGroupId);
        }
        
        return alarmService.selectRuleByApplicationId(applicationId);
    }

    @PreAuthorize("hasPermission(#rule.getApplicationId(), null, T(com.navercorp.pinpoint.web.controller.AlarmController).EDIT_ALARM_ONLY_MANAGER)")
    @PutMapping(value={"/alarmRule", "/application/alarmRule"})
    public Map<String, String> updateRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            return getErrorStringMap("500", "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }
        alarmService.updateRule(rule);
        return getResultStringMap("SUCCESS");
    }

    @PreAuthorize("hasPermission(#rule.getApplicationId(), null, T(com.navercorp.pinpoint.web.controller.AlarmController).EDIT_ALARM_ONLY_MANAGER)")
    @PutMapping(value={"/alarmRuleWithWebhooks", "/application/alarmRuleWithWebhooks"})
    public Map<String, String> updateRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            return getErrorStringMap("500", "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }

        if (!webhookEnable) {
            return getErrorStringMap("500", "webhook should be enabled to bind webhook to an alarm");
        }

        alarmService.updateRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return getResultStringMap("SUCCESS");
    }
    
    @GetMapping(value={"/application/alarmRule/checker", "/alarmRule/checker"})
    public List<String> getCheckerName() {
        return CheckerCategory.getNames();
    }
    
    @ExceptionHandler(Exception.class)
    public Map<String, String> handleException(Exception e) {
        logger.warn(" Exception occurred while trying to CRUD Alarm Rule information", e);
        return getErrorStringMap("500", String.format("Exception occurred while trying to Alarm Rule information: %s", e.getMessage()));
    }

    static private class RuleWithWebhooks {
        private Rule rule;
        private List<String> webhookIds;

        public void setRule(Rule rule) { this.rule = rule; }
        public Rule getRule() { return rule; }
        public void setWebhookIds(List<String> webhookIds) { this.webhookIds = webhookIds; }
        public List<String> getWebhookIds() { return webhookIds; }
    }
}
