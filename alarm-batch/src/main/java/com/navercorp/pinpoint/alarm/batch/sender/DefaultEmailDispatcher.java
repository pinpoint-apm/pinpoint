/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.alarm.batch.sender;

import com.navercorp.pinpoint.alarm.sender.AlarmSendException;
import com.navercorp.pinpoint.alarm.sender.EmailDispatcher;

import jakarta.mail.Message;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Sends an alarm email through a configured {@link JavaMailSender}.
 *
 * <p>An address the mail library cannot parse is dropped with a warning rather than failing
 * the send: the recipients come from a user group, and one member with a bad address would
 * otherwise silence the alarm for everyone else in it. If that leaves nobody at all, the send
 * fails instead -- a delivery recorded as sent to an empty list reads as a working alarm.
 */
public class DefaultEmailDispatcher implements EmailDispatcher {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final JavaMailSender mailSender;
    private final String senderAddress;

    public DefaultEmailDispatcher(JavaMailSender mailSender, String senderAddress) {
        this.mailSender = Objects.requireNonNull(mailSender, "mailSender");
        this.senderAddress = Objects.requireNonNull(senderAddress, "senderAddress");
    }

    @Override
    public void send(List<String> recipients, String subject, String body) throws Exception {
        InternetAddress[] addresses = toInternetAddresses(recipients);
        if (addresses.length == 0) {
            // Returning quietly would have the dispatcher mark the delivery SENT, so the
            // history would show a notification nobody could have received and the only
            // record of the truth would be this log line. Retrying cannot help -- the
            // addresses will not parse next time either -- so it is raised as permanent.
            throw new AlarmSendException(
                    "No address in the group could be parsed, so nobody can receive this: subject="
                            + subject);
        }

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(senderAddress);
        message.setRecipients(Message.RecipientType.TO, addresses);
        // Explicit, like the body below: without it the subject is encoded in the platform
        // charset, which on a container with no locale set is US-ASCII -- every non-ascii
        // character in the rule or application name becomes a question mark.
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setContent(body, "text/html; charset=UTF-8");

        mailSender.send(message);
        logger.info("Sent alarm email: subject={}, recipients={}", subject, addresses.length);
    }

    private InternetAddress[] toInternetAddresses(List<String> emails) {
        List<InternetAddress> addresses = new ArrayList<>(emails.size());
        for (String email : emails) {
            try {
                InternetAddress address = new InternetAddress(email);
                // The constructor parses without checking the result is a whole mailbox, so
                // something like "not-an-email" comes back as a valid object. Left in, the
                // relay refuses the entire message and the members with good addresses lose
                // the alarm as well -- the opposite of dropping one bad address.
                address.validate();
                addresses.add(address);
            } catch (AddressException e) {
                logger.warn("Invalid email address: {}", email, e);
            }
        }
        return addresses.toArray(new InternetAddress[0]);
    }
}
