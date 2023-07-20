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

package com.navercorp.pinpoint.web.webhook.service;

import com.navercorp.pinpoint.common.server.alram.event.DeleteRuleEvent;
import com.navercorp.pinpoint.web.webhook.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.webhook.model.WebhookSendInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

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
    private final WebhookSendInfoDao webhookSendInfoDao;

    public WebhookAlarmServiceImpl(WebhookSendInfoDao webhookSendInfoDao) {
        this.webhookSendInfoDao = Objects.requireNonNull(webhookSendInfoDao, "webhookSendInfoDao");
    }

    @TransactionalEventListener
    @Override
    public void handleDeleteRule(DeleteRuleEvent deleteRule) {
        this.logger.debug("handleDeleteRule:{}", deleteRule);

        if (deleteRule.isWebhookSend()) {
            this.webhookSendInfoDao.deleteWebhookSendInfoByRuleId(deleteRule.getRoleId());
        }
    }


    @Override
    public String insertRuleWithWebhooks(String ruleId, List<String> webhookIds) {
        for (String webhookId : webhookIds) {
            webhookSendInfoDao.insertWebhookSendInfo(new WebhookSendInfo("", webhookId, ruleId));
        }
        return ruleId;
    }



    @Override
    public void updateRuleWithWebhooks(String ruleId, List<String> webhookIds) {
        List<WebhookSendInfo> oldListofWebhookInfos = webhookSendInfoDao.selectWebhookSendInfoByRuleId(ruleId);

        for (WebhookSendInfo webhookSendInfo : oldListofWebhookInfos) {
            // remove already existing webhook mapping to this alarm from webhookIds
            if (!webhookIds.remove(webhookSendInfo.getWebhookId())) {
                // webhook not linked to this alarm anymore, so delete from mysql
                webhookSendInfoDao.deleteWebhookSendInfo(webhookSendInfo);
            }
        }

        // adds newly mapped webhooks to this alarm
        for (String webhookId : webhookIds) {
            WebhookSendInfo webhookSendInfo = new WebhookSendInfo("", webhookId, ruleId);
            webhookSendInfoDao.insertWebhookSendInfo(webhookSendInfo);
        }
    }

}
