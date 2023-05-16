package com.navercorp.pinpoint.batch.alarm.vo;

public class CheckerResult {
    
    private int historyId;
    private String applicationId;
    private String checkerName;
    private String ruleId;
    private boolean detected;
    private int sequenceCount;
    private int timingCount;

    public CheckerResult() {
    }
    
    public CheckerResult(String ruleId, String applicationId, String checkerName, boolean detected, int sequenceCount, int timingCount) {
        this.ruleId = ruleId;
        this.applicationId = applicationId;
        this.checkerName = checkerName;
        this.detected = detected;
        this.sequenceCount = sequenceCount;
        this.timingCount = timingCount;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public int getHistoryId() {
        return historyId;
    }
    
    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public String getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public String getCheckerName() {
        return checkerName;
    }
    
    public void setCheckerName(String checkerName) {
        this.checkerName = checkerName;
    }
    
    public boolean isDetected() {
        return detected;
    }
    
    public void setDetected(boolean detected) {
        this.detected = detected;
    }
    
    public int getSequenceCount() {
        return sequenceCount;
    }
    
    public void setSequenceCount(int sequenceCount) {
        this.sequenceCount = sequenceCount;
    }
    
    public int getTimingCount() {
        return timingCount;
    }
    
    public void setTimingCount(int timingCount) {
        this.timingCount = timingCount;
    }

    public void increseCount() {
        ++sequenceCount;
        
        if (sequenceCount == timingCount) {
            timingCount = sequenceCount * 2 + 1;
        }
    }
}
