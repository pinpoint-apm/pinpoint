package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;

public class WebhookSenderEmptyImpl implements WebhookSender {
    @Override
    public void sendWebhook(AlarmCheckerInterface checker, int sequenceCount) {
        // empty
    }

    @Override
    public void sendWebhook(PinotAlarmCheckerInterface checker, int index) {
        // empty
    }
}
