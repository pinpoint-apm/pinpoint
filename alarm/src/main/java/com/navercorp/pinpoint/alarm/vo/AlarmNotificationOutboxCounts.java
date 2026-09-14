package com.navercorp.pinpoint.alarm.vo;

public record AlarmNotificationOutboxCounts(int total, int sent, int dead) {

    public int pending() {
        return Math.max(0, total - sent - dead);
    }
}
