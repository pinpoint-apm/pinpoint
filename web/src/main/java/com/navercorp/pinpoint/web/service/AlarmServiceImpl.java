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

import com.navercorp.pinpoint.common.server.alram.event.DeleteRuleEvent;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import org.springframework.context.ApplicationEventPublisher;
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
public class AlarmServiceImpl implements AlarmService {
    private final AlarmDao alarmDao;
    private final ApplicationEventPublisher eventPublisher;

    public AlarmServiceImpl(AlarmDao alarmDao, ApplicationEventPublisher eventPublisher) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher");
    }


    @Override
    public String insertRule(Rule rule) {
        return alarmDao.insertRuleExceptWebhookSend(rule);
    }

    
    @Override
    public void deleteRule(Rule rule) {
        alarmDao.deleteRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());

        DeleteRuleEvent event = new DeleteRuleEvent(rule.getRuleId(), rule.isWebhookSend());
        eventPublisher.publishEvent(event);
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
    @Transactional(readOnly = true)
    public List<String> selectApplicationId() {
        return alarmDao.selectApplicationId();
    }
    
    @Override
    public void updateRule(Rule rule) {
        alarmDao.updateRuleExceptWebhookSend(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());
    }

    @Override
    public void updateUserGroupIdOfRule(UserGroup userGroup) {
        alarmDao.updateUserGroupIdOfRule(userGroup);
    }
    
}
