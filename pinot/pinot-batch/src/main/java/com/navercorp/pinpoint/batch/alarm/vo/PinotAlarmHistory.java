package com.navercorp.pinpoint.batch.alarm.vo;

import java.sql.Timestamp;
import java.util.Objects;

public class PinotAlarmHistory {
    private final String ruleId;
    private final Timestamp timestamp;

    public PinotAlarmHistory(String ruleId, long timestamp) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.timestamp = new Timestamp(timestamp);
    }

    public PinotAlarmHistory(String ruleId, Timestamp timestamp) {
        this.ruleId = Objects.requireNonNull(ruleId, "ruleId");
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
    }

    public String getRuleId() {
        return ruleId;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
