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
package com.navercorp.pinpoint.alarm.dao.mapper;

import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleDetailsEntity;
import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntity;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AlarmRuleV2Mapper {

    void insertRule(AlarmRuleEntity rule);

    int updateRule(AlarmRuleEntity rule);

    void updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

    void deleteRule(Long id);

    List<Long> selectRuleIdsByApplication(@Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);

    void deleteByIds(@Param("ruleIds") List<Long> ruleIds);

    AlarmRuleEntity selectRuleById(Long id);

    AlarmRuleDetailsEntity selectRuleDetailsById(Long id);

    AlarmRuleEntity selectRuleByIdForUpdate(Long id);

    List<AlarmRuleEntity> selectRulesByTemplateIdAndApplicationForUpdate(
            @Param("templateId") Long templateId,
            @Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);

    List<AlarmApplication> selectAppliedApplicationsForUpdate(@Param("templateId") Long templateId);

    List<AlarmRuleEntity> selectEnabledRules();

    List<AlarmRuleEntity> selectEnabledRulesAfter(@Param("afterId") long afterId, @Param("limit") int limit);

    List<AlarmRuleEntity> selectDueEnabledRulesAfter(@Param("afterId") long afterId,
                                                     @Param("limit") int limit,
                                                     @Param("now") LocalDateTime now,
                                                     @Param("dataSources") Collection<String> dataSources);

    List<AlarmRuleEntity> selectRulesByApplication(@Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);
}
