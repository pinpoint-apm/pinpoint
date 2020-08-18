package com.navercorp.pinpoint.web.alarm.vo;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;

public class WebhookPayload {
    
    private String pinpointUrl;
    private String batchEnv;
    private Integer sequenceCount;
    private String applicationId;
    private String checkerName;
    private Integer threshold;
    private String userGroupId;
    private String notes;
    private CheckerValue checkerValue;
    
    public WebhookPayload(AlarmChecker checker, BatchConfiguration batchConfiguration, int sequenceCount) {
        this.pinpointUrl = batchConfiguration.getPinpointUrl();
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.sequenceCount = sequenceCount;
        this.applicationId = checker.getRule().getApplicationId();
        this.checkerName = checker.getRule().getCheckerName();
        this.threshold = checker.getRule().getThreshold();
        this.userGroupId = checker.getRule().getUserGroupId();
        this.notes = checker.getRule().getNotes();
        this.checkerValue = checker.getCheckerValue();
    }
    
    public String getPinpointUrl() {
        return pinpointUrl;
    }
    
    public String getBatchEnv() {
        return batchEnv;
    }
    
    public Integer getSequenceCount() {
        return sequenceCount;
    }
    
    public String getApplicationId() {
        return applicationId;
    }
    
    public String getCheckerName() {
        return checkerName;
    }
    
    public Integer getThreshold() {
        return threshold;
    }
    
    public String getUserGroupId() {
        return userGroupId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public CheckerValue getCheckerValue() {
        return checkerValue;
    }
}
