package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class EmailAlarmSenderTest {

    // Who receives it is decided at send time, so an empty group must still enqueue --
    // otherwise a member joining before dispatch has nothing left to deliver, and the
    // event carries no delivery result to audit.
    @Test
    void prepareEnqueuesEvenWhenTheGroupIsEmptyNow() {
        UserGroupEmailResolver resolver = mock(UserGroupEmailResolver.class);
        EmailDispatcher dispatcher = mock(EmailDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        EmailAlarmSender sender = new EmailAlarmSender(resolver, dispatcher, messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("group-empty");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(resolver.resolveEmails("group-empty")).thenReturn(List.of());
        when(messageFormatter.formatTitle(rule, null, metricResults)).thenReturn("title");
        when(messageFormatter.formatHtmlBody(rule, null, metricResults)).thenReturn("body");

        AlarmDeliveryPayload payload = sender.prepare(
                rule, channel, AlarmNotificationChannelConfig.empty(), metricResults);

        assertNotNull(payload, "an empty group must not drop the delivery");
        assertEquals(List.of("group-empty"), payload.destinations());
    }

    @Test
    void prepareUsesCustomTitleAndTemplateAndRemovesSubjectLineBreaks() {
        UserGroupEmailResolver emailResolver = mock(UserGroupEmailResolver.class);
        EmailDispatcher emailDispatcher = mock(EmailDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        EmailAlarmSender sender = new EmailAlarmSender(emailResolver, emailDispatcher, messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("group-email");
        AlarmNotificationChannelConfig config = new AlarmNotificationChannelConfig(
                null, "custom-title", "custom-template");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(emailResolver.resolveEmails("group-email")).thenReturn(List.of("user@example.test"));
        when(messageFormatter.formatTitle(rule, "custom-title", metricResults))
                .thenReturn("rendered\r\nsubject");
        when(messageFormatter.formatHtmlBody(rule, "custom-template", metricResults))
                .thenReturn("<p>rendered-body</p>");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        assertEquals(AlarmDeliveryPayload.forEmail(
                "rendered  subject",
                "<p>rendered-body</p>",
                List.of("group-email")), payload);
        assertFalse(payload.subject().contains("\r"));
        assertFalse(payload.subject().contains("\n"));
        verify(messageFormatter).formatTitle(rule, "custom-title", metricResults);
        verify(messageFormatter).formatHtmlBody(rule, "custom-template", metricResults);
        verifyNoInteractions(emailDispatcher);
    }

    @Test
    void sendUsesPreparedContentWithoutReformatting() throws Exception {
        UserGroupEmailResolver emailResolver = mock(UserGroupEmailResolver.class);
        EmailDispatcher emailDispatcher = mock(EmailDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        EmailAlarmSender sender = new EmailAlarmSender(emailResolver, emailDispatcher, messageFormatter);
        when(emailResolver.resolveEmails("group-email")).thenReturn(List.of("user@example.test"));
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setId(9001L);
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forEmail(
                "persisted-subject",
                "<p>persisted-body</p>",
                List.of("group-email"));

        sender.send(delivery, payload);

        verify(emailDispatcher).send(
                List.of("user@example.test"),
                "persisted-subject",
                "<p>persisted-body</p>");
    }
}
