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
import com.navercorp.pinpoint.alarm.dao.entity.AlarmTemplateItemEntity;
import com.navercorp.pinpoint.alarm.dao.mapper.AlarmTemplateItemMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;

import java.util.List;
import java.util.Objects;

public class AlarmTemplateItemDaoImpl implements AlarmTemplateItemDao {

    private final AlarmTemplateItemMapper mapper;
    private final AlarmRuleEntityMapper entityMapper;

    public AlarmTemplateItemDaoImpl(AlarmTemplateItemMapper mapper, AlarmRuleEntityMapper entityMapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.entityMapper = Objects.requireNonNull(entityMapper, "entityMapper");
    }

    @Override
    public void insert(AlarmTemplateItem item) {
        AlarmTemplateItemEntity entity = entityMapper.toEntity(item);
        mapper.insert(entity);
        item.setId(entity.getId());
    }

    @Override
    public int update(AlarmTemplateItem item) {
        return mapper.update(entityMapper.toEntity(item));
    }

    @Override
    public int markDeleted(Long id) {
        return mapper.markDeleted(id);
    }

    @Override
    public int markDeletedByTemplateId(Long templateId) {
        return mapper.markDeletedByTemplateId(templateId);
    }

    @Override
    public AlarmTemplateItem selectById(Long id) {
        return entityMapper.toModel(mapper.selectById(id));
    }

    @Override
    public AlarmTemplateItem selectByIdForUpdate(Long id) {
        return entityMapper.toModel(mapper.selectByIdForUpdate(id));
    }

    @Override
    public List<AlarmTemplateItem> selectByIds(List<Long> ids) {
        return toModels(mapper.selectByIds(ids));
    }

    @Override
    public List<AlarmTemplateItem> selectByTemplateId(Long templateId) {
        return toModels(mapper.selectByTemplateId(templateId));
    }

    @Override
    public List<AlarmTemplateItem> selectByTemplateIds(List<Long> templateIds) {
        return toModels(mapper.selectByTemplateIds(templateIds));
    }

    @Override
    public int countRulesByTemplateItemId(Long templateItemId) {
        return mapper.countRulesByTemplateItemId(templateItemId);
    }

    private List<AlarmTemplateItem> toModels(List<AlarmTemplateItemEntity> source) {
        return source.stream().map(entityMapper::toModel).toList();
    }
}
