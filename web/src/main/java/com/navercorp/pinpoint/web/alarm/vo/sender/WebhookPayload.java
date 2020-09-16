package com.navercorp.pinpoint.web.alarm.vo.sender;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.alarm.vo.CheckerDetectedValue;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;

public class WebhookPayload {
    
    private String pinpointUrl;
    private String batchEnv;
    private Integer sequenceCount;
    private String applicationId;
    private String checkerName;
    private Integer threshold;
    private String notes;
    private String checkerType;
    private UserGroupMemberPayload userGroupMemberPayload;
    private CheckerDetectedValue checkerDetectedValue;
    
    public WebhookPayload(AlarmChecker checker, BatchConfiguration batchConfiguration, int sequenceCount, UserGroupMemberPayload userGroupMemberPayload) {
        this.pinpointUrl = batchConfiguration.getPinpointUrl();
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.sequenceCount = sequenceCount;
        this.applicationId = checker.getRule().getApplicationId();
        this.checkerName = checker.getRule().getCheckerName();
        this.threshold = checker.getRule().getThreshold();
        this.notes = checker.getRule().getNotes();
        this.checkerType = checker.getCheckerType();
        this.userGroupMemberPayload = userGroupMemberPayload;
        this.checkerDetectedValue = checker.getCheckerDetectedValue();
    }
    
    public UserGroupMemberPayload getUserGroupMemberPayload() {
        return userGroupMemberPayload;
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
    
    public String getNotes() {
        return notes;
    }
    
    public String getCheckerType() {
        return checkerType;
    }
    
    public CheckerDetectedValue getCheckerDetectedValue() {
        return checkerDetectedValue;
    }
}
