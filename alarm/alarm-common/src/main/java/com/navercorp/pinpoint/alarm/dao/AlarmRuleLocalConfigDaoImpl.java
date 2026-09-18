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

import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntityMapper;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmRuleLocalConfigMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;

import java.util.List;
import java.util.Objects;

public class AlarmRuleLocalConfigDaoImpl implements AlarmRuleLocalConfigDao {

    private final AlarmRuleLocalConfigMapper mapper;
    private final AlarmRuleEntityMapper entityMapper;

    public AlarmRuleLocalConfigDaoImpl(AlarmRuleLocalConfigMapper mapper, AlarmRuleEntityMapper entityMapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.entityMapper = Objects.requireNonNull(entityMapper, "entityMapper");
    }

    @Override
    public void upsert(AlarmRuleLocalConfig config) {
        mapper.upsert(entityMapper.toEntity(config));
    }

    @Override
    public void delete(Long ruleId) {
        mapper.delete(ruleId);
    }

    @Override
    public void deleteByRuleIds(List<Long> ruleIds) {
        mapper.deleteByRuleIds(ruleIds);
    }

    @Override
    public AlarmRuleLocalConfig selectByRuleId(Long ruleId) {
        return entityMapper.toModel(mapper.selectByRuleId(ruleId));
    }

    @Override
    public List<AlarmRuleLocalConfig> selectByRuleIds(List<Long> ruleIds) {
        return mapper.selectByRuleIds(ruleIds).stream().map(entityMapper::toModel).toList();
    }
}
