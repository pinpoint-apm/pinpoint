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
package com.navercorp.pinpoint.web.dao.mysql;

import java.util.List;
import java.util.Objects;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.AlarmDao;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 * @author Jongjin.Bae
 */
@Repository
public class MysqlAlarmDao implements AlarmDao {

    private static final String NAMESPACE = AlarmDao.class.getName() + ".";
    
    private final SqlSessionTemplate sqlSessionTemplate;

    public MysqlAlarmDao(@Qualifier("sqlSessionTemplate") SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = Objects.requireNonNull(sqlSessionTemplate, "sqlSessionTemplate");
    }

    @Override
    public String insertRule(Rule rule) {
        sqlSessionTemplate.insert(NAMESPACE + "insertRule", rule);
        return rule.getRuleId();
    }
    
    @Override
    @Deprecated
    public String insertRuleExceptWebhookSend(Rule rule) {
        sqlSessionTemplate.insert(NAMESPACE + "insertRuleExceptWebhookSend", rule);
        return rule.getRuleId();
    }

    @Override
    public void deleteRule(Rule rule) {
        sqlSessionTemplate.insert(NAMESPACE + "deleteRule", rule);
    }

    @Override
    public List<Rule> selectRuleByUserGroupId(String userGroupId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectRuleByUserGroupId", userGroupId);
    }
    
    @Override
    public List<Rule> selectRuleByApplicationId(String applicationId) {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectRuleByApplicationId", applicationId);
    }

    @Override
    public List<String> selectApplicationId() {
        return sqlSessionTemplate.selectList(NAMESPACE + "selectApplicationId");
    }

    @Override
    public void updateRule(Rule rule) {
        sqlSessionTemplate.update(NAMESPACE + "updateRule", rule);
    }
    
    @Override
    @Deprecated
    public void updateRuleExceptWebhookSend(Rule rule) {
        sqlSessionTemplate.update(NAMESPACE + "updateRuleExceptWebhookSend", rule);
    }

    @Override
    public void updateUserGroupIdOfRule(UserGroup userGroup) {
        sqlSessionTemplate.update(NAMESPACE + "updateUserGroupIdOfRule", userGroup);
    }

    @Override
    public void deleteCheckerResult(String ruleId) {
        sqlSessionTemplate.delete(NAMESPACE + "deleteCheckerResult", ruleId);
    }


}
