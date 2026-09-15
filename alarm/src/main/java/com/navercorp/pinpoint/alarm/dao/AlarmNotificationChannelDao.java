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

import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmNotificationChannelDao {

    void insert(AlarmNotificationChannel channel);

    void update(AlarmNotificationChannel channel);

    void delete(Long id);

    AlarmNotificationChannel selectById(Long id);

    AlarmNotificationChannel selectByIdForUpdate(Long id);

    List<AlarmNotificationChannel> selectAll();

    List<AlarmNotificationChannel> selectByService(@Param("serviceName") String serviceName);

    List<AlarmNotificationChannel> selectByIds(List<Long> ids);

    List<AlarmNotificationChannel> selectByIdsWithUsage(List<Long> ids);

    List<AlarmNotificationChannel> selectByRuleId(Long ruleId);

    List<AlarmNotificationChannel> selectByServiceName(@Param("serviceName") String serviceName);

    boolean existsUserGroup(@Param("userGroupId") String userGroupId);

    List<AlarmNotificationChannel> selectByUserGroupDestinationForUpdate(
            @Param("userGroupId") String userGroupId);

    void deleteByUserGroupDestination(@Param("userGroupId") String userGroupId);
}
