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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.event.alram.DeleteRuleEvent;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class WebhookAlarmServiceImpl implements WebhookAlarmService {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AlarmDao alarmDao;
    private final WebhookSendInfoDao webhookSendInfoDao;

    public WebhookAlarmServiceImpl(AlarmDao alarmDao, WebhookSendInfoDao webhookSendInfoDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.webhookSendInfoDao = Objects.requireNonNull(webhookSendInfoDao, "webhookSendInfoDao");
    }

    @EventListener
    public void handleDeleteRule(DeleteRuleEvent deleteRule) {
        this.logger.debug("handleDeleteRule:{}", deleteRule);

        if (deleteRule.isWebhookSend()) {
            this.webhookSendInfoDao.deleteWebhookSendInfoByRuleId(deleteRule.getRoleId());
        }
    }

    @Override
    public String insertRuleWithWebhooks(Rule rule, List<String> webhookIds) {
        String ruleId = alarmDao.insertRule(rule);

        for (String webhookId : webhookIds) {
            webhookSendInfoDao.insertWebhookSendInfo(new WebhookSendInfo("", webhookId, ruleId));
        }

        return ruleId;
    }


    @Override
    public void updateRuleWithWebhooks(Rule rule, List<String> webhookIds) {
        alarmDao.updateRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());

        List<WebhookSendInfo> oldListofWebhookInfos = webhookSendInfoDao.selectWebhookSendInfoByRuleId(rule.getRuleId());

        for (WebhookSendInfo webhookSendInfo : oldListofWebhookInfos) {
            // remove already existing webhook mapping to this alarm from webhookIds
            if (!webhookIds.remove(webhookSendInfo.getWebhookId())) {
                // webhook not linked to this alarm anymore, so delete from mysql
                webhookSendInfoDao.deleteWebhookSendInfo(webhookSendInfo);
            }
        }

        // adds newly mapped webhooks to this alarm
        for (String webhookId : webhookIds) {
            webhookSendInfoDao.insertWebhookSendInfo(new WebhookSendInfo("", webhookId, rule.getRuleId()));
        }
    }

}
