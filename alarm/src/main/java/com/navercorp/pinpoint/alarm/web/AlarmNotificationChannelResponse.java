package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;

import java.time.LocalDateTime;

public record AlarmNotificationChannelResponse(
        Long id,
        String serviceName,
        String channelName,
        AlarmMethodType methodType,
        String destination,
        JsonNode config,
        LocalDateTime updatedAt,
        String webhookAlias,
        String webhookUrl,
        int templateCount,
        int affectedRuleCount,
        int enabledAffectedRuleCount
) {
}
