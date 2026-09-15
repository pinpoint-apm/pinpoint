/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
