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

import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.web.alarm.CheckerCategory;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.response.SuccessResponse;
import com.navercorp.pinpoint.web.service.AlarmService;
import com.navercorp.pinpoint.web.service.WebhookSendInfoService;
import com.navercorp.pinpoint.web.response.AlarmResponse;
import com.navercorp.pinpoint.web.response.Response;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
public class AlarmController {
    private final Logger logger = LogManager.getLogger(this.getClass());

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

    private Response getAlarmResponse(String result, String ruleId) {
        return new AlarmResponse(result, ruleId);
    }

    private boolean isRuleDataValidForPost(Rule rule) {
        if (StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId()) || Objects.isNull(rule.getThreshold())) {
            return false;
        }
        return true;
    }

    @PostMapping()
    public Response insertRule(@RequestBody Rule rule) {
        if (!isRuleDataValidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        String ruleId = alarmService.insertRule(rule);
        return getAlarmResponse("SUCCESS", ruleId);
    }

    @DeleteMapping()
    public Response deleteRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId to delete alarm rule");
        }
        alarmService.deleteRule(rule);
        return SuccessResponse.ok();
    }
    
    @GetMapping()
    public Object getRule(@RequestParam(value=USER_GROUP_ID, required=false) String userGroupId, @RequestParam(value=APPLICATION_ID, required=false) String applicationId) {
        if (StringUtils.isEmpty(userGroupId) && StringUtils.isEmpty(applicationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not userGroupId or applicationID to get alarm rule");
        }
        if (StringUtils.hasLength(userGroupId)) {
            return alarmService.selectRuleByUserGroupId(userGroupId);
        }
        return alarmService.selectRuleByApplicationId(applicationId);
    }

    @PutMapping()
    public Response updateRule(@RequestBody Rule rule) {
        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }
        alarmService.updateRule(rule);
        return SuccessResponse.ok();
    }

    @PostMapping(value = "/includeWebhooks")
    public Response insertRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (!isRuleDataValidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook should be enabled to bind webhook to an alarm");
        }
        String ruleId = alarmService.insertRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return getAlarmResponse("SUCCESS", ruleId);
    }

    @PutMapping(value = "/includeWebhooks")
    public Response updateRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }
        if (!webhookEnable) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "webhook should be enabled to bind webhook to an alarm");
        }
        alarmService.updateRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return SuccessResponse.ok();
    }
    
    @GetMapping(value = "/checker")
    public List<String> getCheckerName() {
        return CheckerCategory.getNames();
    }

    static public class RuleWithWebhooks {
        private Rule rule;
        private List<String> webhookIds;

        public void setRule(Rule rule) { this.rule = rule; }
        public Rule getRule() { return rule; }
        public void setWebhookIds(List<String> webhookIds) { this.webhookIds = webhookIds; }
        public List<String> getWebhookIds() { return webhookIds; }
    }
}
