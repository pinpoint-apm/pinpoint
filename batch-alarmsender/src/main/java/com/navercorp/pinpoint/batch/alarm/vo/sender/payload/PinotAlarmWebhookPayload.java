package com.navercorp.pinpoint.batch.alarm.vo.sender.payload;

import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;

import java.math.BigDecimal;

public class PinotAlarmWebhookPayload {
    private final String pinpointUrl;
    private final String batchEnv;
    private final UserGroup userGroup;
    private final PinotAlarmData data;

    public PinotAlarmWebhookPayload(String pinpointUrl, String batchEnv, PinotAlarmCheckerInterface checker, int index, UserGroup userGroup) {
        this.pinpointUrl = pinpointUrl;
        this.batchEnv = batchEnv;
        this.userGroup = userGroup;

        this.data = new PinotAlarmData(checker, index);
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public PinotAlarmData getData() {
        return data;
    }

    private class PinotAlarmData {
        private final String serviceName;
        private final String applicationName;
        private final String target;
        private final String checkerName;
        private final String unit;

        private final Number detectedValue;
        private final String condition;
        private final BigDecimal threshold;
        private final String notes;

        PinotAlarmData(PinotAlarmCheckerInterface checker, int index) {
            this.serviceName = checker.getServiceName();
            this.applicationName = checker.getApplicationName();
            this.target = checker.getTarget();
            this.checkerName = checker.getCheckerName(index);
            this.unit = checker.getUnit();

            this.detectedValue = checker.getCollectedValue();       //TODO; check output
            this.condition = checker.getAlarmConditionText(index);
            this.threshold = checker.getThreshold(index);
            this.notes = checker.getNotes(index);
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getTarget() {
            return target;
        }

        public String getCheckerName() {
            return checkerName;
        }

        public String getUnit() {
            return unit;
        }

        public Number getDetectedValue() {
            return detectedValue;
        }

        public String getCondition() {
            return condition;
        }

        public BigDecimal getThreshold() {
            return threshold;
        }

        public String getNotes() {
            return notes;
        }
    }
}
