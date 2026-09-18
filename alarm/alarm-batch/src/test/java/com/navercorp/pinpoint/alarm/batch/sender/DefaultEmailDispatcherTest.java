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
import jakarta.mail.Message;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultEmailDispatcherTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final DefaultEmailDispatcher dispatcher =
            new DefaultEmailDispatcher(mailSender, "alarm@example.test");

    // One member with a bad address must not silence the alarm for the rest of the group.
    @Test
    void anUnparsableAddressIsDroppedAndTheRestStillGetIt() throws Exception {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        dispatcher.send(List.of("@@bad", "user@example.test"), "subject", "<b>body</b>");

        verify(mailSender).send(message);
    }

    // "not-an-email" parses into an InternetAddress without complaint, and left in the
    // recipient list the relay refuses the whole message -- so the members with good
    // addresses would lose the alarm because of someone else's typo.
    @Test
    void anAddressThatParsesButIsNotAMailboxIsDroppedToo() throws Exception {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        dispatcher.send(List.of("not-an-email", "user@example.test"), "subject", "<b>body</b>");

        verify(mailSender).send(message);
        verify(message).setRecipients(eq(Message.RecipientType.TO),
                aryEq(new InternetAddress[]{new InternetAddress("user@example.test")}));
    }

    /**
     * Returning quietly here would have the dispatcher record the delivery as sent, so the
     * alarm's history would show a notification that reached nobody. Retrying cannot help --
     * the addresses will not start parsing -- so it has to be a permanent failure.
     */
    @Test
    void nobodyReachableIsAFailureRatherThanASilentDelivery() {
        assertThrows(AlarmSendException.class,
                () -> dispatcher.send(List.of("@@bad", "also@@bad"), "subject", "<b>body</b>"));

        verify(mailSender, never()).createMimeMessage();
    }

    // Subject and body both name their charset; the platform default would mangle non-ascii.
    @Test
    void subjectAndBodyDeclareTheirCharset() throws Exception {
        MimeMessage message = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(message);

        dispatcher.send(List.of("user@example.test"), "[CRITICAL] 에러 급증", "<b>body</b>");

        verify(message).setSubject("[CRITICAL] 에러 급증", "UTF-8");
        verify(message).setContent("<b>body</b>", "text/html; charset=UTF-8");
    }
}
