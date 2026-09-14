package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmRuleLocalConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmRuleLocalConfigDao {

    void upsert(AlarmRuleLocalConfig config);

    void delete(Long ruleId);

    void deleteByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    AlarmRuleLocalConfig selectByRuleId(Long ruleId);

    List<AlarmRuleLocalConfig> selectByRuleIds(@Param("ruleIds") List<Long> ruleIds);
}
