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
