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

package com.navercorp.pinpoint.web.webhook.facade;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.webhook.service.WebhookAlarmService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class WebhookAlarmServiceFacadeImpl implements WebhookAlarmServiceFacade {

    private final WebhookAlarmService webhookAlarmService;

    private final AlarmDao alarmDao;

    public WebhookAlarmServiceFacadeImpl(WebhookAlarmService webhookAlarmService, AlarmDao alarmDao) {
        this.webhookAlarmService = Objects.requireNonNull(webhookAlarmService, "webhookAlarmService");
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
    }

    @Override
    public String insertRuleWithWebhooks(Rule rule, List<String> webhookIds) {
        String ruleId = alarmDao.insertRule(rule);

        webhookAlarmService.insertRuleWithWebhooks(ruleId, webhookIds);

        return ruleId;
    }


    @Override
    public void updateRuleWithWebhooks(Rule rule, List<String> webhookIds) {
        alarmDao.updateRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());

        webhookAlarmService.updateRuleWithWebhooks(rule.getRuleId(), webhookIds);
    }
}
