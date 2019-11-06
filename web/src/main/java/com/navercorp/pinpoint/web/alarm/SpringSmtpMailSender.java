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
package com.navercorp.pinpoint.web.alarm;

import com.navercorp.pinpoint.web.alarm.checker.AlarmChecker;
import com.navercorp.pinpoint.web.batch.BatchConfiguration;
import com.navercorp.pinpoint.web.service.UserGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
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

    private static final InternetAddress[] EMPTY_RECEIVERS = new InternetAddress[0];
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserGroupService userGroupService;
    private final String batchEnv;
    private final String pinpointUrl;
    private final InternetAddress senderEmailAddress;
    private final JavaMailSenderImpl springMailSender;

    public SpringSmtpMailSender(BatchConfiguration batchConfiguration, UserGroupService userGroupService, JavaMailSenderImpl springMailSender) {
        Objects.requireNonNull(batchConfiguration, "batchConfiguration");
        Objects.requireNonNull(userGroupService, "userGroupService");
        Objects.requireNonNull(springMailSender, "mailSender");

        this.pinpointUrl = batchConfiguration.getPinpointUrl();
        this.batchEnv = batchConfiguration.getBatchEnv();
        this.userGroupService = userGroupService;
        this.springMailSender = springMailSender;

        try {
            senderEmailAddress = new InternetAddress(batchConfiguration.getSenderEmailAddress());
        } catch (AddressException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendEmail(AlarmChecker checker, int sequenceCount, StepExecution stepExecution) {
        List<String> receivers = userGroupService.selectEmailOfMember(checker.getuserGroupId());

        if (receivers.size() == 0) {
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
        }catch(Exception e){
            logger.error("can't send alarm email. {}", checker.getRule(), e);
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
