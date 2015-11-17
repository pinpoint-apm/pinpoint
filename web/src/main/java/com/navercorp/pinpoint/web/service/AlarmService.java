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
import java.util.Map;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.CheckerResult;
import com.navercorp.pinpoint.web.alarm.vo.Rule;
import com.navercorp.pinpoint.web.vo.UserGroup;

/**
 * @author minwoo.jung
 */
public interface AlarmService {

    String insertRule(Rule rule);

    void deleteRule(Rule rule);

    List<Rule> selectRuleByUserGroupId(String userGroupId);

    List<Rule> selectRuleByApplicationId(String applicationId);

    void updateRule(Rule rule);

    Map<String, CheckerResult> selectBeforeCheckerResults(String applicationId);

    void updateBeforeCheckerResult(CheckerResult beforeCheckerResult, AlarmChecker checker);

    void deleteRuleByUserGroupId(String groupId);

    void updateUserGroupIdOfRule(UserGroup userGroup);
}
