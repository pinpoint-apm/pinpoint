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
package com.navercorp.pinpoint.alarm.vo;

import java.time.LocalDateTime;
import java.util.Objects;

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;

public class AlarmHistoryV2 {

    private Long id;
    private Long ruleId;
    private AlarmEventType eventType; // FIRED, RESOLVED, CHECK_FAILED
    private String message;
    private String context; // JSON string
    private LocalDateTime createdAt;

    public AlarmHistoryV2() {
    }

    public static AlarmHistoryV2 fired(Long ruleId, String message, String context) {
        return event(ruleId, AlarmEventType.FIRED, message,
                Objects.requireNonNull(context, "context"));
    }

    public static AlarmHistoryV2 resolved(Long ruleId, String message) {
        return event(ruleId, AlarmEventType.RESOLVED, message, null);
    }

    public static AlarmHistoryV2 checkFailed(Long ruleId, String message, String context) {
        return event(ruleId, AlarmEventType.CHECK_FAILED, message,
                Objects.requireNonNull(context, "context"));
    }

    private static AlarmHistoryV2 event(Long ruleId,
                                        AlarmEventType eventType,
                                        String message,
                                        String context) {
        AlarmHistoryV2 history = new AlarmHistoryV2();
        history.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        history.eventType = Objects.requireNonNull(eventType, "eventType");
        history.message = truncate(Objects.requireNonNull(message, "message"));
        history.context = context;
        return history;
    }

    /**
     * A CHECK_FAILED message carries a backend's exception text, which has no length of its
     * own. An insert that overflows the column rolls back the state update with it, leaving
     * the rule due again and failing the same way on every tick.
     */
    private static String truncate(String message) {
        int max = AlarmValidationConstants.MAX_HISTORY_MESSAGE_LENGTH;
        return message.length() <= max ? message : message.substring(0, max - 3) + "...";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public AlarmEventType getEventType() {
        return eventType;
    }

    public void setEventType(AlarmEventType eventType) {
        this.eventType = eventType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
