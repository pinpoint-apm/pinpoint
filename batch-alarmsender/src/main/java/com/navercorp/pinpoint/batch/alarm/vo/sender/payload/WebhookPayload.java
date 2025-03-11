/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.batch.alarm.vo.sender.payload;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.web.vo.RuleInterface;

/**
 * @author Jongjin.Bae
 */
@JsonSerialize(using = WebhookPayloadSerializer.class)
public class WebhookPayload {
    
    private final String pinpointUrl;
    private final String batchEnv;
    private final String applicationName;
    private final String serviceType;
    private final String checkerName;
    private final String checkerType;
    private final UserGroup userGroup;
    private final CheckerDetectedValue checkerDetectedValue;
    private final String unit;
    private final Number threshold;
    private final String notes;
    private final Integer sequenceCount;
    
    public WebhookPayload(String pinpointUrl, String batchEnv, AlarmCheckerInterface checker, int sequenceCount, UserGroup userGroup) {
        this.pinpointUrl = pinpointUrl;
        this.batchEnv = batchEnv;

        final RuleInterface rule = checker.getRule();
        this.applicationName = rule.getApplicationName();
        this.serviceType = rule.getServiceType();
        this.checkerName = rule.getCheckerName();
        this.checkerType = checker.getCheckerType();
        this.userGroup = userGroup;
        this.checkerDetectedValue = checker.getCheckerDetectedValue();
        this.unit = checker.getUnit();
        this.threshold = rule.getThreshold();
        this.notes = rule.getNotes();
        this.sequenceCount = sequenceCount;
    }
    
    public String getPinpointUrl() {
        return pinpointUrl;
    }
    
    public String getBatchEnv() {
        return batchEnv;
    }

    /**
     * @deprecated Since 3.1.0. Use {@link #getApplicationName()} instead.
     */
    @Deprecated
    public String getApplicationId() {
        return getApplicationName();
    }

    public String getApplicationName() {
        return applicationName;
    }
    
    public String getServiceType() {
        return serviceType;
    }
    
    public String getCheckerName() {
        return checkerName;
    }
    
    public String getCheckerType() {
        return checkerType;
    }
    
    public UserGroup getUserGroup() {
        return userGroup;
    }
    
    public CheckerDetectedValue getCheckerDetectedValue() {
        return checkerDetectedValue;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public Number getThreshold() {
        return threshold;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public Integer getSequenceCount() {
        return sequenceCount;
    }
    
    @Override
    public String toString() {
        return "WebhookPayload{" +
                "pinpointUrl='" + pinpointUrl + '\'' +
                ", batchEnv='" + batchEnv + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", checkerName='" + checkerName + '\'' +
                ", checkerType='" + checkerType + '\'' +
                ", userGroup=" + userGroup +
                ", checkerDetectedValue=" + checkerDetectedValue +
                ", unit='" + unit + '\'' +
                ", threshold=" + threshold +
                ", notes='" + notes + '\'' +
                ", sequenceCount=" + sequenceCount +
                '}';
    }
}
