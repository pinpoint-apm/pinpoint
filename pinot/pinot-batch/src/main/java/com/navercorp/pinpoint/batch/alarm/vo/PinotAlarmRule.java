/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.batch.alarm.vo;

import java.math.BigDecimal;

public class PinotAlarmRule {
    private String id;
    private String serviceName;
    private String applicationName;
    private String categoryName;
    private String checkerName;
    private String target;
    private String condition;
    private BigDecimal threshold;
    private String baseline;    // TODO: implement conditions with baselines
    private String userGroupId;
    private boolean smsSend;
    private boolean emailSend;
    private boolean webhookSend;
    private String notes;

    public PinotAlarmRule() {
    }

    public PinotAlarmRule(String serviceName, String applicationName, String categoryName,
                          String checkerName, String target, String condition, BigDecimal threshold, String baseline,
                          String userGroupId, boolean smsSend, boolean emailSend, boolean webhookSend, String notes) {
        this.serviceName = serviceName;
        this.applicationName = applicationName;
        this.categoryName = categoryName;
        this.checkerName = checkerName;
        this.target = target;
        this.condition = condition;
        this.threshold = threshold;
        this.baseline = baseline;
        this.userGroupId = userGroupId;
        this.smsSend = smsSend;
        this.emailSend = emailSend;
        this.webhookSend = webhookSend;
        this.notes = notes;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }

    public String getCheckerName() {
        return this.checkerName;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTarget() {
        return this.target;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCondition() {
        return this.condition;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public String getBaseline() {
        return baseline;
    }

    public void setBaseline(String baseline) {
        this.baseline = baseline;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public boolean isSmsSend() {
        return smsSend;
    }

    public void setSmsSend(boolean smsSend) {
        this.smsSend = smsSend;
    }

    public boolean isEmailSend() {
        return emailSend;
    }

    public void setEmailSend(boolean emailSend) {
        this.emailSend = emailSend;
    }

    public boolean isWebhookSend() {
        return webhookSend;
    }

    public void setWebhookSend(boolean webhookSend) {
        this.webhookSend = webhookSend;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Rule{");
        sb.append("ruleId='").append(id).append('\'');
        sb.append(", serviceName='").append(serviceName).append('\'');
        sb.append(", applicationName='").append(applicationName).append('\'');
        sb.append(", categoryName='").append(categoryName).append('\'');
        sb.append(", checkerName='").append(checkerName).append('\'');
        sb.append(", target='").append(target).append('\'');
        sb.append(", condition='").append(condition).append('\'');
        sb.append(", threshold=").append(threshold);
        sb.append(", userGroupId='").append(userGroupId).append('\'');
        sb.append(", smsSend=").append(smsSend);
        sb.append(", emailSend=").append(emailSend);
        sb.append(", webhookSend=").append(webhookSend);
        sb.append(", notes='").append(notes).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
