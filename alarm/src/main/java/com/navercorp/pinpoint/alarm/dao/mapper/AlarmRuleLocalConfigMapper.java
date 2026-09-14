package com.navercorp.pinpoint.alarm.dao.mapper;

import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleLocalConfigEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmRuleLocalConfigMapper {

    void upsert(AlarmRuleLocalConfigEntity config);

    void delete(Long ruleId);

    void deleteByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    AlarmRuleLocalConfigEntity selectByRuleId(Long ruleId);

    List<AlarmRuleLocalConfigEntity> selectByRuleIds(@Param("ruleIds") List<Long> ruleIds);
}
