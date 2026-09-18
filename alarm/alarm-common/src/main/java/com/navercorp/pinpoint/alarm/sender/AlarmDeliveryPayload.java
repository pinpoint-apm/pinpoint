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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;

import java.util.List;
import java.util.Objects;

/**
 * Immutable, transport-ready notification data stored in the outbox.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlarmDeliveryPayload(String subject,
                                   String content,
                                   List<String> destinations) {

    public AlarmDeliveryPayload {
        destinations = destinations == null ? null : List.copyOf(destinations);
    }

    public static AlarmDeliveryPayload forEmail(String subject,
                                                String content,
                                                List<String> destinations) {
        return new AlarmDeliveryPayload(
                Objects.requireNonNull(subject, "subject"),
                Objects.requireNonNull(content, "content"),
                destinationsOrEmpty(destinations));
    }

    public static AlarmDeliveryPayload forSms(String content, List<String> destinations) {
        return new AlarmDeliveryPayload(
                null,
                Objects.requireNonNull(content, "content"),
                destinationsOrEmpty(destinations));
    }

    public static AlarmDeliveryPayload forWebhook(String content, String destination) {
        return new AlarmDeliveryPayload(
                null,
                Objects.requireNonNull(content, "content"),
                List.of(Objects.requireNonNull(destination, "destination")));
    }

    public void validateFor(AlarmMethodType methodType) {
        Objects.requireNonNull(methodType, "methodType");
        switch (methodType) {
            case EMAIL -> {
                requireField(subject, methodType, "subject");
                requireField(content, methodType, "content");
                requireField(destinations, methodType, "destinations");
            }
            case SMS -> {
                requireField(content, methodType, "content");
                requireField(destinations, methodType, "destinations");
            }
            case WEBHOOK -> {
                requireField(content, methodType, "content");
                requireField(destinations, methodType, "destinations");
                if (destinations.size() != 1) {
                    throw new AlarmSendException(
                            "WEBHOOK delivery payload must have exactly one destination");
                }
            }
        }
    }

    private static List<String> destinationsOrEmpty(List<String> destinations) {
        return destinations == null ? List.of() : destinations;
    }

    private static void requireField(Object value, AlarmMethodType methodType, String fieldName) {
        if (value == null) {
            throw new AlarmSendException(
                    "Missing " + methodType + " delivery payload field: " + fieldName);
        }
    }
}
