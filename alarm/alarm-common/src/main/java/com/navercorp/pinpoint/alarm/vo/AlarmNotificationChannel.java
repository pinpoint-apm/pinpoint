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

import com.navercorp.pinpoint.alarm.validation.AlarmValidationConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class AlarmNotificationChannel {

    private Long id;

    // Request bodies omit this value; controllers set it from the pServiceName header.
    @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
    private String serviceName;

    @NotBlank(message = "channelName must not be blank")
    private String channelName;

    @NotNull(message = "methodType must not be null")
    private AlarmMethodType methodType; // EMAIL, SMS, WEBHOOK

    @NotBlank(message = "destination must not be blank")
    private String destination; // user_group_id or webhook URL

    // JSON string: format (WEBHOOK), title and template (message customization)
    private String config;
    private LocalDateTime updatedAt;
    private String webhookAlias;
    private String webhookUrl;
    private int templateCount;
    private int affectedRuleCount;
    private int enabledAffectedRuleCount;

    public AlarmNotificationChannel() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public AlarmMethodType getMethodType() {
        return methodType;
    }

    public void setMethodType(AlarmMethodType methodType) {
        this.methodType = methodType;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getWebhookAlias() {
        return webhookAlias;
    }

    public void setWebhookAlias(String webhookAlias) {
        this.webhookAlias = webhookAlias;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public int getTemplateCount() {
        return templateCount;
    }

    public void setTemplateCount(int templateCount) {
        this.templateCount = templateCount;
    }

    public int getAffectedRuleCount() {
        return affectedRuleCount;
    }

    public void setAffectedRuleCount(int affectedRuleCount) {
        this.affectedRuleCount = affectedRuleCount;
    }

    public int getEnabledAffectedRuleCount() {
        return enabledAffectedRuleCount;
    }

    public void setEnabledAffectedRuleCount(int enabledAffectedRuleCount) {
        this.enabledAffectedRuleCount = enabledAffectedRuleCount;
    }

    @Override
    public String toString() {
        return "AlarmNotificationChannel{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", channelName='" + channelName + '\'' +
                ", methodType=" + methodType +
                ", destination='" + destination + '\'' +
                '}';
    }
}
