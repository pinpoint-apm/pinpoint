package com.navercorp.pinpoint.alarm.vo;

import java.time.LocalDateTime;

public class AlarmState {

    private Long ruleId;
    private AlarmStatus status; // NORMAL, FIRING, CHECK_FAILED
    private LocalDateTime lastCheckedAt;
    private LocalDateTime lastFiredAt;
    private LocalDateTime lastNotificationEnqueuedAt;
    private LocalDateTime lastNotifiedAt;
    private LocalDateTime nextCheckAt;

    public AlarmState() {
    }

    public AlarmState(Long ruleId) {
        this.ruleId = ruleId;
        this.status = AlarmStatus.NORMAL;
    }

    public boolean isFiring() {
        return status == AlarmStatus.FIRING;
    }

    public boolean isCheckFailed() {
        return status == AlarmStatus.CHECK_FAILED;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public AlarmStatus getStatus() {
        return status;
    }

    public void setStatus(AlarmStatus status) {
        this.status = status;
    }

    public LocalDateTime getLastCheckedAt() {
        return lastCheckedAt;
    }

    public void setLastCheckedAt(LocalDateTime lastCheckedAt) {
        this.lastCheckedAt = lastCheckedAt;
    }

    public LocalDateTime getLastFiredAt() {
        return lastFiredAt;
    }

    public void setLastFiredAt(LocalDateTime lastFiredAt) {
        this.lastFiredAt = lastFiredAt;
    }

    public LocalDateTime getLastNotifiedAt() {
        return lastNotifiedAt;
    }

    public LocalDateTime getLastNotificationEnqueuedAt() {
        return lastNotificationEnqueuedAt;
    }

    public void setLastNotificationEnqueuedAt(LocalDateTime lastNotificationEnqueuedAt) {
        this.lastNotificationEnqueuedAt = lastNotificationEnqueuedAt;
    }

    public void setLastNotifiedAt(LocalDateTime lastNotifiedAt) {
        this.lastNotifiedAt = lastNotifiedAt;
    }

    public LocalDateTime getNextCheckAt() {
        return nextCheckAt;
    }

    public void setNextCheckAt(LocalDateTime nextCheckAt) {
        this.nextCheckAt = nextCheckAt;
    }
}
