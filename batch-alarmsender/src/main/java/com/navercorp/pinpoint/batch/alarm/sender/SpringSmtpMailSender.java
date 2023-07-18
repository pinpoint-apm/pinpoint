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
package com.navercorp.pinpoint.batch.alarm.sender;

import com.navercorp.pinpoint.batch.alarm.AlarmSenderProperties;
import com.navercorp.pinpoint.batch.alarm.checker.AlarmCheckerInterface;
import com.navercorp.pinpoint.batch.alarm.checker.PinotAlarmCheckerInterface;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SpringSmtpMailSender implements MailSender {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final UserGroupService userGroupService;
    private final String batchEnv;
    private final String pinpointUrl;
    private final InternetAddress senderEmailAddress;
    private final JavaMailSenderImpl springMailSender;

    public SpringSmtpMailSender(AlarmSenderProperties alarmSenderProperties, UserGroupService userGroupService, JavaMailSenderImpl springMailSender) {
        Objects.requireNonNull(alarmSenderProperties, "alarmSenderProperties");
        this.pinpointUrl = alarmSenderProperties.getPinpointUrl();
        this.batchEnv = alarmSenderProperties.getBatchEnv();

        this.userGroupService = Objects.requireNonNull(userGroupService, "userGroupService");
        this.springMailSender = Objects.requireNonNull(springMailSender, "mailSender");

        try {
            this.senderEmailAddress = new InternetAddress(alarmSenderProperties.getSenderEmailAddress());
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmail(AlarmCheckerInterface checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getUserGroupId());

        if (receivers.isEmpty()) {
            return;
        }

        try{
            AlarmMailTemplate mailTemplate = new AlarmMailTemplate(checker, pinpointUrl, batchEnv, sequenceCount);
            MimeMessage message = springMailSender.createMimeMessage();
            message.setFrom(senderEmailAddress);
            message.setRecipients(Message.RecipientType.TO, getReceivers(receivers));

            final String subject =  mailTemplate.createSubject();
            message.setSubject(subject);
            message.setContent(mailTemplate.createBody(), "text/html");
            springMailSender.send(message);
            logger.info("send email : {}", subject);
        } catch(Exception e) {
            logger.error("can't send alarm email. {}", checker.toString(), e);
        }
    }

    @Override
    public void sendEmail(PinotAlarmCheckerInterface checker, int index) {
        String userGroupId = checker.getUserGroupId(index);
        List<String> receivers = userGroupService.selectEmailOfMember(userGroupId);
        if (receivers.isEmpty()) {
            return;
        }

        try{
            PinotAlarmMailTemplate mailTemplate = new PinotAlarmMailTemplate(pinpointUrl, batchEnv, checker, index);
            MimeMessage message = springMailSender.createMimeMessage();
            message.setFrom(senderEmailAddress);
            message.setRecipients(Message.RecipientType.TO, getReceivers(receivers));

            final String subject =  mailTemplate.createSubject();
            message.setSubject(subject);
            message.setContent(mailTemplate.createBody(), "text/html");
            springMailSender.send(message);
            logger.info("send email : {}", subject);
        } catch(Exception e) {
            logger.error("can't send alarm email. {}", checker.toString(), e);
        }
    }

    private InternetAddress[] getReceivers(List<String> receivers) throws AddressException {
        InternetAddress[] receiverArray = new InternetAddress[receivers.size()];
        int index = 0;
        for (String receiver : receivers) {
            receiverArray[index++] = new InternetAddress(receiver);
        }

        return receiverArray;
    }
}
