/*
 * Copyright 2023 NAVER Corp.
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
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.response.AlarmResponse;
import com.navercorp.pinpoint.web.response.Response;
import com.navercorp.pinpoint.web.response.SuccessResponse;
import com.navercorp.pinpoint.web.service.WebhookAlarmService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@RestController
@RequestMapping(value = {"/alarmRule", "/application/alarmRule"})
@ConditionalOnProperty(name = "webhook.enable", havingValue = "true")
public class WebhookAlarmController {
    private final WebhookAlarmService webhookAlarmService;

    public WebhookAlarmController(WebhookAlarmService webhookAlarmService) {
        this.webhookAlarmService = Objects.requireNonNull(webhookAlarmService, "webhookAlarmService");
    }


    @PostMapping(value = "/includeWebhooks")
    public AlarmResponse insertRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (!Rule.isRuleDataValidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }
        String ruleId = webhookAlarmService.insertRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return new AlarmResponse("SUCCESS", ruleId);
    }

    @PutMapping(value = "/includeWebhooks")
    public Response updateRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (StringUtils.isEmpty(rule.getRuleId()) || StringUtils.isEmpty(rule.getApplicationId()) || StringUtils.isEmpty(rule.getCheckerName()) || StringUtils.isEmpty(rule.getUserGroupId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }
        webhookAlarmService.updateRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return SuccessResponse.ok();
    }

    static public class RuleWithWebhooks {
        private Rule rule;
        private List<String> webhookIds;

        public void setRule(Rule rule) {
            this.rule = rule;
        }

        public Rule getRule() {
            return rule;
        }

        public void setWebhookIds(List<String> webhookIds) {
            this.webhookIds = webhookIds;
        }

        public List<String> getWebhookIds() {
            return webhookIds;
        }
    }
}
