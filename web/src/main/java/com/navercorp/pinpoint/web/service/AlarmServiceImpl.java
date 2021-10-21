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
package com.navercorp.pinpoint.web.service;

import java.util.List;
import java.util.Objects;

import com.navercorp.pinpoint.web.dao.WebhookSendInfoDao;
import com.navercorp.pinpoint.web.vo.WebhookSendInfo;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class AlarmServiceImpl implements AlarmService {
    private final AlarmDao alarmDao;
    private final WebhookSendInfoDao webhookSendInfoDao;

    public AlarmServiceImpl(AlarmDao alarmDao, WebhookSendInfoDao webhookSendInfoDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.webhookSendInfoDao = Objects.requireNonNull(webhookSendInfoDao, "webhookSendInfoDao");
    }
    
    @Override
    public String insertRule(Rule rule) {
        return alarmDao.insertRule(rule);
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
    public void deleteRule(Rule rule) {
        alarmDao.deleteRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());
        if (rule.isWebhookSend()) {
            webhookSendInfoDao.deleteWebhookSendInfoByRuleId(rule.getRuleId());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Rule> selectRuleByUserGroupId(String userGroupId) {
        return alarmDao.selectRuleByUserGroupId(userGroupId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Rule> selectRuleByApplicationId(String applicationId) {
        return alarmDao.selectRuleByApplicationId(applicationId);
    }
    
    @Override
    public void updateRule(Rule rule) {
        alarmDao.updateRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());
    }

    @Override
    public void updateRuleWithWebhooks(Rule rule, List<String> webhookIds) {
        updateRule(rule);

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

    @Override
    public void deleteRuleByUserGroupId(String groupId) {
        alarmDao.deleteRuleByUserGroupId(groupId);
    }
    
    @Override
    public void updateUserGroupIdOfRule(UserGroup userGroup) {
        alarmDao.updateUserGroupIdOfRule(userGroup);
    }
    
}
