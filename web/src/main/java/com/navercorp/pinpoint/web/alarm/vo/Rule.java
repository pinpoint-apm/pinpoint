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

package com.navercorp.pinpoint.web.alarm.vo;

import com.navercorp.pinpoint.web.vo.RuleInterface;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;

/**
 * @author minwoo.jung
 */
public class Rule implements RuleInterface {

    private String ruleId;
    private String applicationName;
    private String serviceType;
    @NotBlank private String checkerName;
    @NotNull private Integer threshold;
    @NotBlank private String userGroupId;
    private boolean smsSend;
    private boolean emailSend;
    private boolean webhookSend;
    private String notes;

    public Rule() {
    }

    public Rule(String applicationName, String serviceType, String checkerName, Integer Threshold, String userGroupId,
                boolean smsSend, boolean emailSend, boolean webhookSend, String notes) {
        this.applicationName = applicationName;
        this.serviceType = serviceType;
        this.checkerName = checkerName;
        this.threshold = Threshold;
        this.userGroupId = userGroupId;
        this.smsSend = smsSend;
        this.emailSend = emailSend;
        this.webhookSend = webhookSend;
        this.notes = notes;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public String getCheckerName() {
        return checkerName;
    }

    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }

    @Override
    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    @Override
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public static boolean isRuleInvalidForPost(Rule rule) {
        return StringUtils.isEmpty(rule.getApplicationName()) ||
                StringUtils.isEmpty(rule.getCheckerName()) ||
                StringUtils.isEmpty(rule.getUserGroupId()) ||
                rule.getThreshold() == null;
    }


    public static boolean isRuleInvalid(Rule rule) {
        return StringUtils.isEmpty(rule.getRuleId()) ||
                StringUtils.isEmpty(rule.getApplicationName()) ||
                StringUtils.isEmpty(rule.getCheckerName()) ||
                StringUtils.isEmpty(rule.getUserGroupId()) ||
                rule.getThreshold() == null;
    }


    @Override
    public String toString() {
        return "Rule{" + "ruleId='" + ruleId + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", checkerName='" + checkerName + '\'' +
                ", threshold=" + threshold +
                ", userGroupId='" + userGroupId + '\'' +
                ", smsSend=" + smsSend +
                ", emailSend=" + emailSend +
                ", webhookSend=" + webhookSend +
                ", notes='" + notes + '\'' +
                '}';
    }
}
