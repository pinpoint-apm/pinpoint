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

/**
 * @author minwoo.jung
 */
public class Rule {

    private String ruleId;
    private String applicationId;
    private String serviceType;
    private String checkerName;
    private Integer threshold;
    private String userGroupId;
    private boolean smsSend;
    private boolean emailSend;
    private String notes;

    public Rule() {
    }

    public Rule(String applicationId, String serviceType, String checkerName, Integer Threshold, String userGroupId, boolean smsSend, boolean emailSend, String notes) {
        this.applicationId = applicationId;
        this.serviceType = serviceType;
        this.checkerName = checkerName;
        this.threshold = Threshold;
        this.userGroupId = userGroupId;
        this.smsSend = smsSend;
        this.emailSend = emailSend;
        this.notes = notes;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    
    public String getCheckerName() {
        return checkerName;
    }

    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setuserGroupId(String userGroupId) {
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

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
