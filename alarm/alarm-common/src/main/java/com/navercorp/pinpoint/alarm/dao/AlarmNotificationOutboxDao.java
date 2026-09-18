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

import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutboxCounts;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AlarmNotificationOutboxDao {

    void insert(AlarmNotificationOutbox delivery);

    List<Long> selectClaimCandidateIds(@Param("now") LocalDateTime now,
                                       @Param("limit") int limit);

    int claimAvailable(@Param("claimToken") String claimToken,
                       @Param("now") LocalDateTime now,
                       @Param("availableAt") LocalDateTime availableAt,
                       @Param("ids") List<Long> ids);

    List<AlarmNotificationOutbox> selectByClaimToken(String claimToken);

    int markSent(@Param("id") Long id,
                 @Param("claimToken") String claimToken);

    int markRetry(@Param("id") Long id,
                  @Param("claimToken") String claimToken,
                  @Param("availableAt") LocalDateTime availableAt);

    int markDead(@Param("id") Long id,
                 @Param("claimToken") String claimToken);

    int deleteByRuleId(Long ruleId);

    int deleteByRuleIds(@Param("ruleIds") List<Long> ruleIds);

    AlarmNotificationOutboxCounts selectCountsByHistoryId(Long historyId);
}
