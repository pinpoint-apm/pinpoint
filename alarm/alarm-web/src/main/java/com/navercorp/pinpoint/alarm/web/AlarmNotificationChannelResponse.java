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
package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.alarm.util.json.UtcTimestampSerializer;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;

import java.time.LocalDateTime;

public record AlarmNotificationChannelResponse(
        Long id,
        String serviceName,
        String channelName,
        AlarmMethodType methodType,
        String destination,
        JsonNode config,
        @JsonSerialize(using = UtcTimestampSerializer.class) LocalDateTime updatedAt,
        String webhookAlias,
        String webhookUrl,
        int templateCount,
        int affectedRuleCount,
        int enabledAffectedRuleCount
) {
}
