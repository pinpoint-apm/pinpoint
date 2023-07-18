package com.navercorp.pinpoint.batch.alarm;

import org.springframework.beans.factory.annotation.Value;

public class AlarmSenderProperties {
    @Value("${webhook.enable}")
    private boolean webhookEnable;

    @Value("${alarm.mail.server.url}")
    private String emailServerUrl;

    @Value("${alarm.mail.sender.address}")
    private String senderEmailAddress;

    @Value("${pinpoint.url}")
    private String pinpointUrl;

    @Value("${batch.server.env}")
    private String batchEnv;

    public boolean isWebhookEnable() {
        return webhookEnable;
    }

    public String getPinpointUrl() {
        return pinpointUrl;
    }

    public String getEmailServerUrl() {
        return emailServerUrl;
    }

    public String getSenderEmailAddress() {
        return senderEmailAddress;
    }

    public String getBatchEnv() {
        return batchEnv;
    }

    @Override
    public String toString() {
        return "AlarmSenderProperties{" +
                "emailServerUrl='" + emailServerUrl + '\'' +
                ", batchEnv='" + batchEnv + '\'' +
                ", senderEmailAddress='" + senderEmailAddress + '\'' +
                ", pinpointUrl='" + pinpointUrl + '\'' +
                ", webhookEnable=" + webhookEnable +
                '}';
    }
}
