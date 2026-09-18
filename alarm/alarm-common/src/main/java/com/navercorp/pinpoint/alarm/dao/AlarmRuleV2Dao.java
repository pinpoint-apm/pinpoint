/*
 * Copyright 2026 NAVER Corp.
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
 */
package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmRuleV2Dao {

    void insertRule(AlarmRuleV2 rule);


    int updateRule(AlarmRuleV2 rule);


    void updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    void deleteRule(Long id);

    List<Long> selectRuleIdsByApplication(@Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);

    void deleteByIds(@Param("ruleIds") List<Long> ruleIds);

    AlarmRuleV2 selectRuleById(Long id);

    AlarmRuleDetails selectRuleDetailsById(Long id);

    AlarmRuleV2 selectRuleByIdForUpdate(Long id);

    List<AlarmRuleV2> selectRulesByTemplateIdAndApplicationForUpdate(
            @Param("templateId") Long templateId,
            @Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);

    /**
     * Applications the bundle is already applied to, locked so a concurrent apply
     * cannot add one between this read and the rules stamped from it.
     */
    List<AlarmApplication> selectAppliedApplicationsForUpdate(@Param("templateId") Long templateId);

    List<AlarmRuleV2> selectEnabledRules();

    List<AlarmRuleV2> selectEnabledRulesAfter(@Param("afterId") long afterId, @Param("limit") int limit);

    List<AlarmRuleV2> selectDueEnabledRulesAfter(@Param("afterId") long afterId,
                                                 @Param("limit") int limit,
                                                 @Param("now") LocalDateTime now);

    List<AlarmRuleV2> selectRulesByApplication(@Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);
}
