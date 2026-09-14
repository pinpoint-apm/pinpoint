package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmHistoryV2;

import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmHistoryV2Dao {

    void insert(AlarmHistoryV2 history);

    void updateContext(@Param("id") Long id, @Param("context") String context);

    AlarmHistoryV2 selectById(Long id);

    AlarmHistoryV2 selectByIdForUpdate(Long id);

    List<AlarmHistoryV2> selectByRuleId(@Param("ruleId") Long ruleId, @Param("limit") int limit);

    void deleteByRuleId(Long ruleId);

    void deleteByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    int deleteOlderThan(@Param("threshold") LocalDateTime threshold, @Param("limit") int limit);
}
