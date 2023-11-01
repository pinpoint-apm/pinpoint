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
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
public class SpringSmtpMailSender implements MailSender, InitializingBean {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String FROM_KEY = "mail.smtp.from";

    private final UserGroupService userGroupService;
    private final String batchEnv;
    private final String pinpointUrl;
    private final String senderEmailAddress;
    private final JavaMailSenderImpl springMailSender;

    public SpringSmtpMailSender(AlarmSenderProperties alarmSenderProperties,
                                UserGroupService userGroupService,
                                JavaMailSenderImpl springMailSender) {
        Objects.requireNonNull(alarmSenderProperties, "alarmSenderProperties");
        this.pinpointUrl = alarmSenderProperties.getPinpointUrl();
        this.batchEnv = alarmSenderProperties.getBatchEnv();

        this.userGroupService = Objects.requireNonNull(userGroupService, "userGroupService");
        this.springMailSender = Objects.requireNonNull(springMailSender, "springMailSender");

        this.senderEmailAddress = springMailSender.getJavaMailProperties().getProperty(FROM_KEY);
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            new InternetAddress(senderEmailAddress);
        } catch (AddressException e) {
            logger.info("Invalid From Address", e);
        }

        try {
            springMailSender.testConnection();
        } catch (MessagingException e) {
            logger.debug("MailServer TestConnection failed", e);
        }
    }


    @Override
    public void sendEmail(AlarmCheckerInterface checker, int sequenceCount) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getUserGroupId());

        if (receivers.isEmpty()) {
            return;
        }

        try {
            AlarmMailTemplate mailTemplate = new AlarmMailTemplate(checker, pinpointUrl, batchEnv, sequenceCount);

            final MimeMessage message = newMimeMessage(mailTemplate, receivers);

            springMailSender.send(message);
            logger.info("send email : {}", message.getSubject());
        } catch (Exception e) {
            logger.error("can't send alarm email. {}", checker.toString(), e);
        }
    }

    private MimeMessage newMimeMessage(AlarmMailTemplate mailTemplate, List<String> receivers) throws MessagingException {
        MimeMessage message = springMailSender.createMimeMessage();
        message.setFrom(this.senderEmailAddress);
        message.setRecipients(Message.RecipientType.TO, getReceivers(receivers));

        final String subject = mailTemplate.createSubject();
        message.setSubject(subject);
        message.setContent(mailTemplate.createBody(), "text/html");
        return message;
    }

    @Override
    public void sendEmail(PinotAlarmCheckerInterface checker, int index) {
        String userGroupId = checker.getUserGroupId(index);
        List<String> receivers = userGroupService.selectEmailOfMember(userGroupId);
        if (receivers.isEmpty()) {
            return;
        }

        try {
            PinotAlarmMailTemplate mailTemplate = new PinotAlarmMailTemplate(pinpointUrl, batchEnv, checker, index);
            MimeMessage message = springMailSender.createMimeMessage();
            message.setFrom(senderEmailAddress);
            message.setRecipients(Message.RecipientType.TO, getReceivers(receivers));

            final String subject = mailTemplate.createSubject();
            message.setSubject(subject);
            message.setContent(mailTemplate.createBody(), "text/html");
            springMailSender.send(message);
            logger.info("send email : {}", subject);
        } catch (Exception e) {
            logger.error("can't send alarm email. {}", checker.toString(), e);
        }
    }

    private InternetAddress[] getReceivers(List<String> receivers) {
        List<InternetAddress> receiverArray = new ArrayList<>(receivers.size());
        for (final String receiver : receivers) {
            try {
                receiverArray.add(new InternetAddress(receiver));
            } catch (AddressException e) {
                logger.info("address parse error receiver-address:{}", receiver, e);
            }
        }
        return receiverArray.toArray(new InternetAddress[0]);
    }
}
