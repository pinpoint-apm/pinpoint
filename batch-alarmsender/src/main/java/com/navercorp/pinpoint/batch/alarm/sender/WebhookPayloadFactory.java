package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.vo.sender.payload.PinotAlarmWebhookPayload;
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

    public WebhookPayload newPayload(AlarmCheckerInterface checker, int sequenceCount, UserGroup userGroup) {
        return new WebhookPayload(pinpointUrl, batchEnv, checker, sequenceCount, userGroup);
    }

    public PinotAlarmWebhookPayload newPayload(PinotAlarmCheckerInterface checker, int index, UserGroup userGroup) {
        return new PinotAlarmWebhookPayload(pinpointUrl, batchEnv, checker, index, userGroup);
    }
}
