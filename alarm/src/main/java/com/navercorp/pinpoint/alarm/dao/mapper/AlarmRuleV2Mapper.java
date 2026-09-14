package com.navercorp.pinpoint.alarm.dao.mapper;

import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleDetailsEntity;
import com.navercorp.pinpoint.alarm.dao.entity.AlarmRuleEntity;
import com.navercorp.pinpoint.alarm.vo.AlarmApplication;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
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
                                                     @Param("now") LocalDateTime now);

    List<AlarmRuleEntity> selectRulesByApplication(@Param("serviceName") String serviceName,
            @Param("applicationName") String applicationName,
            @Param("applicationType") String applicationType);
}
