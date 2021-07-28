package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmChecker;
import org.springframework.batch.core.StepExecution;

public class WebhookSenderEmptyImpl implements WebhookSender {
    @Override
    public void sendWebhook(AlarmChecker checker, int sequenceCount, StepExecution stepExecution) {
        // empty
    }
}
