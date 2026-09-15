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
package com.navercorp.pinpoint.alarm.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Parses and validates the JSON stored in {@code alarm_notification_channel.config}.
 * Unknown fields are intentionally ignored so transport-specific options can be added later.
 */
@Component
public final class AlarmNotificationChannelConfigParser {

    private static final Set<String> SUPPORTED_WEBHOOK_FORMATS = Set.of("DEFAULT", "SLACK");
    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_TEMPLATE_LENGTH = 2000;

    private final ObjectReader configReader;

    public AlarmNotificationChannelConfigParser(ObjectMapper objectMapper) {
        ObjectMapper requiredObjectMapper = Objects.requireNonNull(objectMapper, "objectMapper");
        this.configReader = requiredObjectMapper.readerFor(JsonNode.class)
                .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
    }

    public AlarmNotificationChannelConfig parse(AlarmMethodType methodType, String config) {
        if (config == null || config.isBlank()) {
            return AlarmNotificationChannelConfig.empty();
        }

        JsonNode root;
        try {
            root = configReader.readValue(config);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                    "notification channel config must be a valid JSON object", e);
        }
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("notification channel config must be a JSON object");
        }

        String format = stringField(root, "format", Integer.MAX_VALUE);
        if (format != null) {
            format = format.toUpperCase(Locale.ROOT);
            if (!SUPPORTED_WEBHOOK_FORMATS.contains(format)) {
                throw new IllegalArgumentException(
                        "notification channel config format must be DEFAULT or SLACK");
            }
            if (methodType != AlarmMethodType.WEBHOOK) {
                throw new IllegalArgumentException(
                        "notification channel config format is only supported for WEBHOOK channels");
            }
        }
        return new AlarmNotificationChannelConfig(
                format,
                stringField(root, "title", MAX_TITLE_LENGTH),
                stringField(root, "template", MAX_TEMPLATE_LENGTH));
    }

    private String stringField(JsonNode root, String fieldName, int maxLength) {
        JsonNode field = root.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (!field.isTextual()) {
            throw new IllegalArgumentException(
                    "notification channel config " + fieldName + " must be a string");
        }
        String value = field.textValue();
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                    "notification channel config " + fieldName + " must be at most " + maxLength + " characters");
        }
        return value;
    }
}
