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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SmsAlarmSenderTest {

    // Who receives it is decided at send time, so an empty group must still enqueue --
    // otherwise a member joining before dispatch has nothing left to deliver, and the
    // event carries no delivery result to audit.
    @Test
    void prepareEnqueuesEvenWhenTheGroupIsEmptyNow() {
        UserGroupPhoneResolver resolver = mock(UserGroupPhoneResolver.class);
        SmsDispatcher dispatcher = mock(SmsDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        SmsAlarmSender sender = new SmsAlarmSender(resolver, dispatcher, messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("group-empty");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(resolver.resolvePhoneNumbers("group-empty")).thenReturn(List.of());
        when(messageFormatter.formatTitle(rule, null, metricResults)).thenReturn("title");
        when(messageFormatter.formatSmsBody(rule, null, metricResults)).thenReturn("body");

        AlarmDeliveryPayload payload = sender.prepare(
                rule, channel, AlarmNotificationChannelConfig.empty(), metricResults);

        assertNotNull(payload, "an empty group must not drop the delivery");
        assertEquals(List.of("group-empty"), payload.destinations());
    }

    @Test
    void prepareUsesCustomTitleAndTemplate() {
        UserGroupPhoneResolver phoneResolver = mock(UserGroupPhoneResolver.class);
        SmsDispatcher smsDispatcher = mock(SmsDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        SmsAlarmSender sender = new SmsAlarmSender(phoneResolver, smsDispatcher, messageFormatter);
        AlarmRuleV2 rule = new AlarmRuleV2();
        AlarmNotificationChannel channel = new AlarmNotificationChannel();
        channel.setDestination("group-sms");
        AlarmNotificationChannelConfig config = new AlarmNotificationChannelConfig(
                null, "custom-title", "custom-template");
        MetricQueryResult metricResults = MetricQueryResult.empty();
        when(phoneResolver.resolvePhoneNumbers("group-sms")).thenReturn(List.of("01012345678"));
        when(messageFormatter.formatTitle(rule, "custom-title", metricResults)).thenReturn("rendered-title");
        when(messageFormatter.formatSmsBody(rule, "custom-template", metricResults)).thenReturn("rendered-body");

        AlarmDeliveryPayload payload = sender.prepare(rule, channel, config, metricResults);

        assertEquals(AlarmDeliveryPayload.forSms(
                "rendered-title\nrendered-body", List.of("group-sms")), payload);
        verify(messageFormatter).formatTitle(rule, "custom-title", metricResults);
        verify(messageFormatter).formatSmsBody(rule, "custom-template", metricResults);
        verifyNoInteractions(smsDispatcher);
    }

    @Test
    void sendUsesPreparedContentWithoutReformatting() throws Exception {
        UserGroupPhoneResolver phoneResolver = mock(UserGroupPhoneResolver.class);
        SmsDispatcher smsDispatcher = mock(SmsDispatcher.class);
        AlarmMessageFormatter messageFormatter = mock(AlarmMessageFormatter.class);
        SmsAlarmSender sender = new SmsAlarmSender(phoneResolver, smsDispatcher, messageFormatter);
        when(phoneResolver.resolvePhoneNumbers("group-sms")).thenReturn(List.of("01012345678"));
        AlarmNotificationOutbox delivery = new AlarmNotificationOutbox();
        delivery.setId(9001L);
        AlarmDeliveryPayload payload = AlarmDeliveryPayload.forSms(
                "persisted-title\npersisted-body", List.of("group-sms"));

        sender.send(delivery, payload);

        verify(smsDispatcher).send(
                List.of("01012345678"), "persisted-title\npersisted-body");
    }
}
