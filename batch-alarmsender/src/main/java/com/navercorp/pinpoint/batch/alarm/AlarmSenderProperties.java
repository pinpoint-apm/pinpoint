package com.navercorp.pinpoint.batch.alarm;

import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

public class AlarmSenderProperties {

    private final String pinpointUrl;

    private final String batchEnv;

    public AlarmSenderProperties(@Value("${spring.mail.pinpoint-url}") String pinpointUrl,
                                 @Value("${batch.server.env}") String batchEnv) {
        this.pinpointUrl = Objects.requireNonNull(pinpointUrl, "pinpointUrl");
        this.batchEnv = Objects.requireNonNull(batchEnv, "batchEnv");
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    @Override
    public String toString() {
        return "AlarmSenderProperties{" +
                ", batchEnv='" + batchEnv + '\'' +
                ", pinpointUrl='" + pinpointUrl + '\'' +
                '}';
    }
}
