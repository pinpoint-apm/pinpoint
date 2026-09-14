package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Alarm template bundle header access. The header has no JSON columns, so this
 * is a plain MyBatis mapper interface bound straight to the VO (like
 * {@link AlarmStateDao}) without an entity/Impl layer.
 */
public interface AlarmTemplateDao {

    void insert(AlarmTemplate template);

    int update(AlarmTemplate template);

    int markDeleted(Long id);

    AlarmTemplate selectById(Long id);

    AlarmTemplate selectByIdForUpdate(Long id);

    List<AlarmTemplate> selectByIds(@Param("ids") List<Long> ids);

    List<AlarmTemplate> selectByService(@Param("serviceName") String serviceName);

    List<AlarmTemplate> selectByServiceAndDataSource(@Param("serviceName") String serviceName,
                                                     @Param("dataSource") String dataSource);
}
