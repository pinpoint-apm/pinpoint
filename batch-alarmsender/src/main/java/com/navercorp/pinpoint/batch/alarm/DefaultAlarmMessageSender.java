/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.batch.alarm;

import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.sender.EmptySmsSender;
import com.navercorp.pinpoint.batch.alarm.sender.MailSender;
import com.navercorp.pinpoint.batch.alarm.sender.SmsSender;
import com.navercorp.pinpoint.batch.alarm.sender.WebhookSender;

import java.util.Objects;
import java.util.Optional;

/**
 * @author minwoo.jung
 */
public class DefaultAlarmMessageSender implements AlarmMessageSender {

    private final MailSender mailSender;

    private final SmsSender smsSender;
    
    private final WebhookSender webhookSender;
    
    public DefaultAlarmMessageSender(MailSender mailSender,
                                     WebhookSender webhookSender,
                                     Optional<SmsSender> smsSender) {
        this.mailSender = Objects.requireNonNull(mailSender, "mailSender");
        this.webhookSender = Objects.requireNonNull(webhookSender, "webhookSender");
        this.smsSender = smsSender.orElseGet(EmptySmsSender::new);
    }

    @Override
    public void sendSms(AlarmCheckerInterface checker, int sequenceCount) {
        this.smsSender.sendSms(checker, sequenceCount);
    }

    @Override
    public void sendEmail(AlarmCheckerInterface checker, int sequenceCount) {
        this.mailSender.sendEmail(checker, sequenceCount);
    }
    
    @Override
    public void sendWebhook(AlarmCheckerInterface checker, int sequenceCount) {
        webhookSender.sendWebhook(checker, sequenceCount);
    }

    @Override
    public void sendSms(PinotAlarmCheckerInterface checker, int index) {
        this.smsSender.sendSms(checker, index);
    }

    @Override
    public void sendEmail(PinotAlarmCheckerInterface checker, int index) {
        this.mailSender.sendEmail(checker, index);
    }

    @Override
    public void sendWebhook(PinotAlarmCheckerInterface checker, int index) {
        webhookSender.sendWebhook(checker, index);
    }
}
