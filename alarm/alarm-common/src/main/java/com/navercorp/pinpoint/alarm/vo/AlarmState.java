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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.alarm.util.json.UtcTimestampSerializer;
import com.navercorp.pinpoint.alarm.util.json.UtcTimestampDeserializer;

import java.time.LocalDateTime;

public class AlarmState {

    private Long ruleId;
    private AlarmStatus status; // NORMAL, FIRING, CHECK_FAILED
    @JsonSerialize(using = UtcTimestampSerializer.class)
    @JsonDeserialize(using = UtcTimestampDeserializer.class)
    private LocalDateTime lastCheckedAt;
    @JsonSerialize(using = UtcTimestampSerializer.class)
    @JsonDeserialize(using = UtcTimestampDeserializer.class)
    private LocalDateTime lastFiredAt;
    @JsonSerialize(using = UtcTimestampSerializer.class)
    @JsonDeserialize(using = UtcTimestampDeserializer.class)
    private LocalDateTime lastNotificationEnqueuedAt;
    @JsonSerialize(using = UtcTimestampSerializer.class)
    @JsonDeserialize(using = UtcTimestampDeserializer.class)
    private LocalDateTime lastNotifiedAt;
    @JsonSerialize(using = UtcTimestampSerializer.class)
    @JsonDeserialize(using = UtcTimestampDeserializer.class)
    private LocalDateTime nextCheckAt;

    public AlarmState() {
    }

    public AlarmState(Long ruleId) {
        this.ruleId = ruleId;
        this.status = AlarmStatus.NORMAL;
    }

    public boolean isFiring() {
        return status == AlarmStatus.FIRING;
    }

    public boolean isCheckFailed() {
        return status == AlarmStatus.CHECK_FAILED;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public LocalDateTime getLastFiredAt() {
        return lastFiredAt;
    }

    public void setLastFiredAt(LocalDateTime lastFiredAt) {
        this.lastFiredAt = lastFiredAt;
    }

    public LocalDateTime getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    public LocalDateTime getLastNotificationEnqueuedAt() {
        return lastNotificationEnqueuedAt;
    }

    public void setLastNotificationEnqueuedAt(LocalDateTime lastNotificationEnqueuedAt) {
        this.lastNotificationEnqueuedAt = lastNotificationEnqueuedAt;
    }

    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }

    public LocalDateTime getNextCheckAt() {
        return nextCheckAt;
    }

    public void setNextCheckAt(LocalDateTime nextCheckAt) {
        this.nextCheckAt = nextCheckAt;
    }
}
