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

public class AlarmNotificationOutbox {

    private Long id;
    private Long historyId;
    private Long channelId;
    private AlarmMethodType methodType;
    private String payload;
    private AlarmNotificationOutboxStatus status;
    private int attemptCount;
    private LocalDateTime availableAt;
    private String claimToken;

    public static AlarmNotificationOutbox pending(Long historyId,
                                                   Long channelId,
                                                   AlarmMethodType methodType,
                                                   String payload,
                                                   LocalDateTime availableAt) {
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.historyId = Objects.requireNonNull(historyId, "historyId");
        delivery.channelId = Objects.requireNonNull(channelId, "channelId");
        delivery.methodType = Objects.requireNonNull(methodType, "methodType");
        delivery.payload = Objects.requireNonNull(payload, "payload");
        delivery.status = AlarmNotificationOutboxStatus.PENDING;
        delivery.attemptCount = 0;
        delivery.availableAt = Objects.requireNonNull(availableAt, "availableAt");
        return delivery;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public AlarmMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(AlarmMethodType methodType) {
        this.methodType = methodType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public AlarmNotificationOutboxStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmNotificationOutboxStatus status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public LocalDateTime getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(LocalDateTime availableAt) {
        this.availableAt = availableAt;
    }

    public String getClaimToken() {
        return claimToken;
    }

    public void setClaimToken(String claimToken) {
        this.claimToken = claimToken;
    }

}
