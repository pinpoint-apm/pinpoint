package com.navercorp.pinpoint.alarm.dao.mapper;

import com.navercorp.pinpoint.alarm.dao.entity.AlarmTemplateItemEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmTemplateItemMapper {

    void insert(AlarmTemplateItemEntity item);

    int update(AlarmTemplateItemEntity item);

    int markDeleted(Long id);

    int markDeletedByTemplateId(Long templateId);

    AlarmTemplateItemEntity selectById(Long id);

    AlarmTemplateItemEntity selectByIdForUpdate(Long id);

    List<AlarmTemplateItemEntity> selectByIds(@Param("ids") List<Long> ids);

    List<AlarmTemplateItemEntity> selectByTemplateId(Long templateId);

    List<AlarmTemplateItemEntity> selectByTemplateIds(@Param("templateIds") List<Long> templateIds);

    int countRulesByTemplateItemId(Long templateItemId);
}
