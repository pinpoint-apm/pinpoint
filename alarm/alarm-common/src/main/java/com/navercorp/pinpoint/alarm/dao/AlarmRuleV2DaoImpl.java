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

import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntity;
import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntityMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleV2Mapper;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleDetails;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AlarmRuleV2DaoImpl implements AlarmRuleV2Dao {

    private final AlarmRuleV2Mapper mapper;
    private final AlarmRuleEntityMapper entityMapper;

    public AlarmRuleV2DaoImpl(AlarmRuleV2Mapper mapper, AlarmRuleEntityMapper entityMapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.entityMapper = Objects.requireNonNull(entityMapper, "entityMapper");
    }

    @Override
    public void insertRule(AlarmRuleV2 rule) {
        AlarmRuleEntity entity = entityMapper.toEntity(rule);
        mapper.insertRule(entity);
        rule.setId(entity.getId());
    }

    @Override
    public int updateRule(AlarmRuleV2 rule) {
        return mapper.updateRule(entityMapper.toEntity(rule));
    }

    @Override
    public void updateEnabled(Long id, boolean enabled) {
        mapper.updateEnabled(id, enabled);
    }

    @Override
    public void deleteRule(Long id) {
        mapper.deleteRule(id);
    }

    @Override
    public List<Long> selectRuleIdsByApplication(
            String serviceName, String applicationName, String applicationType) {
        return mapper.selectRuleIdsByApplication(serviceName, applicationName, applicationType);
    }

    @Override
    public void deleteByIds(List<Long> ruleIds) {
        mapper.deleteByIds(ruleIds);
    }

    @Override
    public AlarmRuleV2 selectRuleById(Long id) {
        return entityMapper.toModel(mapper.selectRuleById(id));
    }

    @Override
    public AlarmRuleDetails selectRuleDetailsById(Long id) {
        return entityMapper.toModel(mapper.selectRuleDetailsById(id));
    }

    @Override
    public AlarmRuleV2 selectRuleByIdForUpdate(Long id) {
        return entityMapper.toModel(mapper.selectRuleByIdForUpdate(id));
    }

    @Override
    public List<AlarmRuleV2> selectRulesByTemplateIdAndApplicationForUpdate(
            Long templateId, String serviceName, String applicationName, String applicationType) {
        return toModels(mapper.selectRulesByTemplateIdAndApplicationForUpdate(
                templateId, serviceName, applicationName, applicationType));
    }

    @Override
    public List<AlarmApplication> selectAppliedApplicationsForUpdate(Long templateId) {
        return mapper.selectAppliedApplicationsForUpdate(templateId);
    }

    @Override
    public List<AlarmRuleV2> selectEnabledRules() {
        return toModels(mapper.selectEnabledRules());
    }

    @Override
    public List<AlarmRuleV2> selectEnabledRulesAfter(long afterId, int limit) {
        return toModels(mapper.selectEnabledRulesAfter(afterId, limit));
    }

    @Override
    public List<AlarmRuleV2> selectDueEnabledRulesAfter(long afterId, int limit, LocalDateTime now,
                                                        Collection<String> dataSources) {
        return toModels(mapper.selectDueEnabledRulesAfter(afterId, limit, now, dataSources));
    }

    @Override
    public List<AlarmRuleV2> selectRulesByApplication(
            String serviceName, String applicationName, String applicationType) {
        return toModels(mapper.selectRulesByApplication(serviceName, applicationName, applicationType));
    }

    private List<AlarmRuleV2> toModels(List<AlarmRuleEntity> source) {
        return source.stream().map(entityMapper::toModel).toList();
    }
}
