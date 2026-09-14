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
 * Renders the message when the outbox row is created and resolves the group's email addresses
 * when it is sent, so a group edited in between reaches the members it has at delivery.
 * A group that resolves to nobody is a delivered notification with no addressee, not a
 * failure to retry.
 */
public class EmailAlarmSender implements AlarmSender {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final UserGroupEmailResolver emailResolver;
    private final EmailDispatcher emailDispatcher;
    private final AlarmMessageFormatter messageFormatter;

    public EmailAlarmSender(UserGroupEmailResolver emailResolver,
                             EmailDispatcher emailDispatcher,
                             AlarmMessageFormatter messageFormatter) {
        this.emailResolver = Objects.requireNonNull(emailResolver, "emailResolver");
        this.emailDispatcher = Objects.requireNonNull(emailDispatcher, "emailDispatcher");
        this.messageFormatter = Objects.requireNonNull(messageFormatter, "messageFormatter");
    }

    @Override
    public AlarmMethodType getMethodType() {
        return AlarmMethodType.EMAIL;
    }

    @Override
    public AlarmDeliveryPayload prepare(AlarmRuleV2 rule,
                                        AlarmNotificationChannel channel,
                                        AlarmNotificationChannelConfig channelConfig,
                                        MetricQueryResult metricResults) {
        // Not resolved here on purpose: who receives this is decided at send time, so a
        // group that is empty now must still enqueue. Gating on it would drop the delivery
        // before a member joins, and leave the event with no delivery result to audit.
        String subject = messageFormatter.formatTitle(rule, channelConfig.title(), metricResults)
                .replaceAll("[\r\n]", " ");
        String htmlBody = messageFormatter.formatHtmlBody(rule, channelConfig.template(), metricResults);
        return AlarmDeliveryPayload.forEmail(subject, htmlBody, List.of(channel.getDestination()));
    }

    @Override
    public void send(AlarmNotificationOutbox delivery, AlarmDeliveryPayload payload) {
        String destinationRef = payload.destinations().isEmpty() ? null : payload.destinations().get(0);
        List<String> emails = destinationRef != null ? emailResolver.resolveEmails(destinationRef) : List.of();

        if (CollectionUtils.isEmpty(emails)) {
            logger.warn("User group resolved to no email recipients: delivery_id={}", delivery.getId());
            return;
        }

        try {
            emailDispatcher.send(emails, payload.subject(), payload.content());
            logger.info("Sent Email alarm: delivery_id={}, recipients={}", delivery.getId(), emails.size());
        } catch (Exception e) {
            logger.error("Failed to send Email alarm: delivery_id={}", delivery.getId(), e);
            throw new AlarmSendException("Failed to send Email alarm: delivery_id=" + delivery.getId(), e);
        }
    }
}
