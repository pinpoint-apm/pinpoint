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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.vo.UserGroup;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author minwoo.jung
 */
@Service
@Transactional(rollbackFor = {Exception.class})
public class AlarmServiceImpl implements AlarmService {

    private final AlarmDao alarmDao;

    public AlarmServiceImpl(AlarmDao alarmDao) {
        this.alarmDao = Objects.requireNonNull(alarmDao, "alarmDao");
    }

    @Override
    public String insertRule(Rule rule) {
        return alarmDao.insertRule(rule);
        
    }

    @Override
    public void deleteRule(Rule rule) {
        alarmDao.deleteRule(rule);
        alarmDao.deleteCheckerResult(rule.getRuleId());
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
    @Transactional(readOnly = true)
    public Map<String, CheckerResult> selectBeforeCheckerResults(String applicationId) {
        Map<String, CheckerResult> checkerResults = new HashMap<>();
        List<CheckerResult> CheckerResultList = alarmDao.selectBeforeCheckerResultList(applicationId);
        
        if (!CheckerResultList.isEmpty()) {
            for (CheckerResult checkerResult : CheckerResultList) {
                checkerResults.put(checkerResult.getRuleId(), checkerResult);
            }
        }
        
        return checkerResults;
    }

    @Override
    public void updateBeforeCheckerResult(CheckerResult beforeCheckerResult, AlarmChecker checker) {
        alarmDao.deleteCheckerResult(beforeCheckerResult.getRuleId());
        
        if (checker.isDetected()) {
            beforeCheckerResult.setDetected(true);
            beforeCheckerResult.increseCount();
            alarmDao.insertCheckerResult(beforeCheckerResult);
        } else {
            alarmDao.insertCheckerResult(new CheckerResult(checker.getRule().getRuleId(), checker.getRule().getApplicationId(), checker.getRule().getCheckerName(), false, 0, 1));
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
