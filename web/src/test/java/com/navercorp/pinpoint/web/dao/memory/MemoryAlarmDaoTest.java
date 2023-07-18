/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.web.dao.memory;

import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.dao.UserGroupDao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author minwoo.jung
 */
public class MemoryAlarmDaoTest {

    @Test
    public void insertDeleteTest() {
        final String applicationId = "applicationId";
        final String groupId = "userGroupId";
        AlarmRule alarmRule = new AlarmRule();
        MemoryAlarmDao memoryAlarmDao = new MemoryAlarmDao(alarmRule, mock(UserGroupDao.class));
        Rule rule = new Rule(applicationId, "serviceType", "checkerName1", 0, groupId, true, true, true, "");
        Rule rule2 = new Rule(applicationId, "serviceType", "checkerName2", 10, groupId, true, true, true, "");
        memoryAlarmDao.insertRule(rule);
        memoryAlarmDao.insertRule(rule2);

        List<Rule> resultRules1 = memoryAlarmDao.selectRuleByApplicationId(applicationId);
        assertThat(resultRules1).hasSize(2);
        resultRules1 = memoryAlarmDao.selectRuleByApplicationId("app");
        assertThat(resultRules1).isEmpty();

        List<Rule> resultRules2 = memoryAlarmDao.selectRuleByUserGroupId(groupId);
        assertThat(resultRules2).hasSize(2);
        resultRules2 = memoryAlarmDao.selectRuleByUserGroupId("id");
        assertThat(resultRules2).isEmpty();

        memoryAlarmDao.deleteRule(rule);
        memoryAlarmDao.deleteRule(rule2);
        List<Rule> resultRules = memoryAlarmDao.selectRuleByApplicationId(applicationId);
        assertThat(resultRules).isEmpty();
    }

}