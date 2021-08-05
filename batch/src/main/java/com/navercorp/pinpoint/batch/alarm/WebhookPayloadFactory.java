package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.UserGroup;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.WebhookPayload;

import java.util.Objects;

public class WebhookPayloadFactory {
    private final String pinpointUrl;
    private final String batchEnv;

    public WebhookPayloadFactory(String pinpointUrl, String batchEnv) {
        this.pinpointUrl = Objects.requireNonNull(pinpointUrl, "pinpointUrl");
        this.batchEnv = Objects.requireNonNull(batchEnv, "batchEnv");
    }

    public WebhookPayload newPayload(AlarmChecker<?> checker, int sequenceCount, UserGroup userGroup) {
        return new WebhookPayload(pinpointUrl, batchEnv, checker, sequenceCount, userGroup);
    }
}
