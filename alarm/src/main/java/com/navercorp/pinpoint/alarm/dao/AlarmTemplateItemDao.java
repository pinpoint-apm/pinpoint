package com.navercorp.pinpoint.alarm.dao;

import com.navercorp.pinpoint.alarm.vo.AlarmTemplateItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmTemplateItemDao {

    void insert(AlarmTemplateItem item);

    int update(AlarmTemplateItem item);

    int markDeleted(Long id);

    int markDeletedByTemplateId(Long templateId);

    AlarmTemplateItem selectById(Long id);

    AlarmTemplateItem selectByIdForUpdate(Long id);

    List<AlarmTemplateItem> selectByIds(@Param("ids") List<Long> ids);

    List<AlarmTemplateItem> selectByTemplateId(Long templateId);

    List<AlarmTemplateItem> selectByTemplateIds(@Param("templateIds") List<Long> templateIds);

    int countRulesByTemplateItemId(Long templateItemId);
}
