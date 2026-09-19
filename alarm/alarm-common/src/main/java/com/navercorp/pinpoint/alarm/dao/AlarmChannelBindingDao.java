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

import com.navercorp.pinpoint.alarm.vo.AlarmChannelBinding;
import com.navercorp.pinpoint.alarm.vo.AlarmChannelOwnerType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AlarmChannelBindingDao {

    void insert(AlarmChannelBinding binding);

    void delete(@Param("ownerType") AlarmChannelOwnerType ownerType,
                @Param("ownerId") Long ownerId,
                @Param("channelId") Long channelId);

    void deleteByOwner(@Param("ownerType") AlarmChannelOwnerType ownerType,
                       @Param("ownerId") Long ownerId);

    void deleteByChannelId(@Param("channelId") Long channelId);

    void deleteRuleBindingsByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    void deleteByUserGroupDestination(@Param("userGroupId") String userGroupId);

    boolean existsByChannelId(@Param("channelId") Long channelId);

    List<AlarmChannelBinding> selectByOwner(@Param("ownerType") AlarmChannelOwnerType ownerType,
                                            @Param("ownerId") Long ownerId);
}
