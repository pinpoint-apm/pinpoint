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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Alarm template bundle header: the identity that applications apply and that
 * alarm_channel_binding (owner_type = 'TEMPLATE') points at. The rule
 * definitions live in {@link AlarmTemplateItem}; {@code items} and the usage
 * counts are assembled by the service layer, not stored on this row.
 */
public class AlarmTemplate {

    private Long id;

    // No @NotBlank: the controller fills this in from the service header, the same
    // way AlarmRuleV2 does, so a request body never carries it.
    @Size(max = AlarmValidationConstants.MAX_APPLICATION_IDENTIFIER_LENGTH, message = "serviceName is too long")
    private String serviceName;

    @NotBlank(message = "name must not be blank")
    @Size(max = AlarmValidationConstants.MAX_RULE_NAME_LENGTH, message = "name is too long")
    private String name;

    @Size(max = AlarmValidationConstants.MAX_RULE_DESCRIPTION_LENGTH, message = "description is too long")
    private String description;

    private LocalDateTime updatedAt;
    private int usedRuleCount;
    private int enabledUsedRuleCount;
    private int usedApplicationCount;
    private int channelCount;
    /**
     * A full replacement of the bundle when it arrives on a write: an item with an
     * id updates that item, an item without one is inserted, and a stored item
     * missing from the list is deleted along with the rules stamped from it.
     */
    @Valid
    @NotEmpty(message = "items must not be empty")
    @Size(max = AlarmValidationConstants.MAX_TEMPLATE_ITEM_COUNT,
            message = "items must contain at most " + AlarmValidationConstants.MAX_TEMPLATE_ITEM_COUNT + " items")
    private List<AlarmTemplateItem> items;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getUsedRuleCount() {
        return usedRuleCount;
    }

    public void setUsedRuleCount(int usedRuleCount) {
        this.usedRuleCount = usedRuleCount;
    }

    public int getEnabledUsedRuleCount() {
        return enabledUsedRuleCount;
    }

    public void setEnabledUsedRuleCount(int enabledUsedRuleCount) {
        this.enabledUsedRuleCount = enabledUsedRuleCount;
    }

    public int getUsedApplicationCount() {
        return usedApplicationCount;
    }

    public void setUsedApplicationCount(int usedApplicationCount) {
        this.usedApplicationCount = usedApplicationCount;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public List<AlarmTemplateItem> getItems() {
        return items;
    }

    public void setItems(List<AlarmTemplateItem> items) {
        this.items = items;
    }
}
