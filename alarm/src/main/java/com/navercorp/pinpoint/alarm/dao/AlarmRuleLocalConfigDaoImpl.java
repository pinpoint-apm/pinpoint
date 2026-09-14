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
