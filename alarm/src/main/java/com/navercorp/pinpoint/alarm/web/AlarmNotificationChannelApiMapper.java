package com.navercorp.pinpoint.alarm.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class AlarmNotificationChannelApiMapper {

    private final ObjectMapper objectMapper;

    public AlarmNotificationChannelApiMapper(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
    }

    public AlarmNotificationChannel toModel(String serviceName, AlarmNotificationChannelRequest request) {
        Objects.requireNonNull(request, "request");
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setServiceName(serviceName);
        channel.setChannelName(request.getChannelName());
        channel.setMethodType(request.getMethodType());
        channel.setDestination(request.getDestination());
        channel.setConfig(toConfigString(request.getConfig()));
        return channel;
    }

    public AlarmNotificationChannelResponse toResponse(AlarmNotificationChannel channel) {
        Objects.requireNonNull(channel, "channel");
        return new AlarmNotificationChannelResponse(
                channel.getId(),
                channel.getServiceName(),
                channel.getChannelName(),
                channel.getMethodType(),
                channel.getDestination(),
                toConfigJson(channel.getConfig()),
                channel.getUpdatedAt(),
                channel.getWebhookAlias(),
                channel.getWebhookUrl(),
                channel.getTemplateCount(),
                channel.getAffectedRuleCount(),
                channel.getEnabledAffectedRuleCount()
        );
    }

    public List<AlarmNotificationChannelResponse> toResponses(List<AlarmNotificationChannel> channels) {
        return channels.stream()
                .map(this::toResponse)
                .toList();
    }

    private String toConfigString(JsonNode config) {
        if (config == null || config.isNull()) {
            return null;
        }
        requireObject(config);
        return config.toString();
    }

    private JsonNode toConfigJson(String config) {
        if (!StringUtils.hasText(config)) {
            return null;
        }
        try {
            JsonNode parsed = objectMapper.readTree(config);
            if (parsed == null || !parsed.isObject()) {
                throw new IllegalStateException("Stored channel config must be a JSON object");
            }
            return parsed;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored channel config is not valid JSON", e);
        }
    }

    private static void requireObject(JsonNode config) {
        if (config == null || !config.isObject()) {
            throw new IllegalArgumentException("config must be a JSON object");
        }
    }
}
