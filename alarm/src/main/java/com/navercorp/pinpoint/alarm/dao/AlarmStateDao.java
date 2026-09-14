package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmState;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

import java.util.List;

public interface AlarmStateDao {

    AlarmState selectByRuleId(Long ruleId);

    void upsert(AlarmState state);

    int updateLastNotifiedAt(@Param("ruleId") Long ruleId,
                             @Param("notifiedAt") LocalDateTime notifiedAt);

    void deleteByRuleId(Long ruleId);

    void deleteByRuleIds(@Param("ruleIds") List<Long> ruleIds);
}
