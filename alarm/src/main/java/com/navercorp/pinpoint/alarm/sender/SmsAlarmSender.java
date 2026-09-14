package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * Renders the message when the outbox row is created and resolves the group's phone numbers
 * when it is sent, so a group edited in between reaches the members it has at delivery.
 * A group that resolves to nobody is a delivered notification with no addressee, not a
 * failure to retry.
 */
public class SmsAlarmSender implements AlarmSender {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final UserGroupPhoneResolver phoneResolver;
    private final SmsDispatcher smsDispatcher;
    private final AlarmMessageFormatter messageFormatter;

    public SmsAlarmSender(UserGroupPhoneResolver phoneResolver,
                           SmsDispatcher smsDispatcher,
                           AlarmMessageFormatter messageFormatter) {
        this.phoneResolver = Objects.requireNonNull(phoneResolver, "phoneResolver");
        this.smsDispatcher = Objects.requireNonNull(smsDispatcher, "smsDispatcher");
        this.messageFormatter = Objects.requireNonNull(messageFormatter, "messageFormatter");
    }

    @Override
    public AlarmMethodType getMethodType() {
        return AlarmMethodType.SMS;
    }

    @Override
    public AlarmDeliveryPayload prepare(AlarmRuleV2 rule,
                                        AlarmNotificationChannel channel,
                                        AlarmNotificationChannelConfig channelConfig,
                                        MetricQueryResult metricResults) {
        // Not resolved here on purpose: who receives this is decided at send time, so a
        // group that is empty now must still enqueue. Gating on it would drop the delivery
        // before a member joins, and leave the event with no delivery result to audit.
        String title = messageFormatter.formatTitle(rule, channelConfig.title(), metricResults);
        String body = messageFormatter.formatSmsBody(rule, channelConfig.template(), metricResults);
        return AlarmDeliveryPayload.forSms(title + "\n" + body, List.of(channel.getDestination()));
    }

    @Override
    public void send(AlarmNotificationOutbox delivery, AlarmDeliveryPayload payload) {
        String destinationRef = payload.destinations().isEmpty() ? null : payload.destinations().get(0);
        List<String> phoneNumbers = destinationRef != null ? phoneResolver.resolvePhoneNumbers(destinationRef) : List.of();

        if (CollectionUtils.isEmpty(phoneNumbers)) {
            logger.warn("User group resolved to no phone numbers: delivery_id={}", delivery.getId());
            return;
        }

        try {
            smsDispatcher.send(phoneNumbers, payload.content());
            logger.info("Sent SMS alarm: delivery_id={}, recipients={}", delivery.getId(), phoneNumbers.size());
        } catch (Exception e) {
            logger.error("Failed to send SMS alarm: delivery_id={}", delivery.getId(), e);
            throw new AlarmSendException("Failed to send SMS alarm: delivery_id=" + delivery.getId(), e);
        }
    }
}
