/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.authorization.controller;

import com.navercorp.pinpoint.common.server.response.Response;
import com.navercorp.pinpoint.common.server.response.Result;
import com.navercorp.pinpoint.common.server.response.SimpleResponse;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.response.AlarmResponse;
import com.navercorp.pinpoint.web.webhook.WebhookModule;
import com.navercorp.pinpoint.web.webhook.facade.WebhookAlarmServiceFacade;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping(value = {"/api/alarmRule", "/api/application/alarmRule"})
@ConditionalOnProperty(name = WebhookModule.NAME, havingValue = "true", matchIfMissing = true)
public class WebhookAlarmController {
    private final WebhookAlarmServiceFacade webhookAlarmServiceFacade;

    public WebhookAlarmController(WebhookAlarmServiceFacade webhookAlarmServiceFacade) {
        this.webhookAlarmServiceFacade = Objects.requireNonNull(webhookAlarmServiceFacade, "webhookAlarmAdaptor");
    }

    @PreAuthorize("hasPermission(#ruleWithWebhooks.getRule().getApplicationName(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    @PostMapping(value = "/includeWebhooks")
    public AlarmResponse insertRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (Rule.isRuleInvalidForPost(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not applicationId/checkerName/userGroupId/threashold to insert alarm rule");
        }

        final String ruleId = webhookAlarmServiceFacade.insertRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return new AlarmResponse(Result.SUCCESS, ruleId);
    }

    @PreAuthorize("hasPermission(#ruleWithWebhooks.getRule().getApplicationName(), null, T(com.navercorp.pinpoint.web.security.PermissionChecker).PERMISSION_ALARM_EDIT_ALARM_ONLY_MANAGER)")
    @PutMapping(value = "/includeWebhooks")
    public Response updateRuleWithWebhooks(@RequestBody RuleWithWebhooks ruleWithWebhooks) {
        Rule rule = ruleWithWebhooks.getRule();
        if (Rule.isRuleInvalid(rule)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "there is not ruleId/userGroupId/applicationid/checkerName to update alarm rule");
        }
        webhookAlarmServiceFacade.updateRuleWithWebhooks(rule, ruleWithWebhooks.getWebhookIds());
        return SimpleResponse.ok();
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
