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
