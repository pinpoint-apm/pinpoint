package com.navercorp.pinpoint.alarm.vo;

public enum AlarmNotificationOutboxStatus {
    PENDING,
    PROCESSING,
    RETRY,
    SENT,
    DEAD
}
