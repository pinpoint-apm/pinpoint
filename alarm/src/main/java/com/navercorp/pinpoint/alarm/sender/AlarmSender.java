package com.navercorp.pinpoint.alarm.sender;

import com.navercorp.pinpoint.alarm.evaluation.MetricQueryResult;
import com.navercorp.pinpoint.alarm.vo.AlarmMethodType;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationChannel;
import com.navercorp.pinpoint.alarm.vo.AlarmNotificationOutbox;
import com.navercorp.pinpoint.alarm.vo.AlarmRuleV2;

/**
 * Renders what a notification says when the outbox row is written, and sends it when the
 * row is claimed.
 *
 * <p>The message is fixed at enqueue time so a rule edited afterwards cannot rewrite a
 * notification that already fired. Who receives it is not: the recipients of a user group
 * are resolved at send time, so a group edited between enqueue and dispatch delivers to
 * the members it has now, which is what an on-call rotation has to do.
 */
public interface AlarmSender {

    /**
     * @return the method type this sender handles (EMAIL, SMS, WEBHOOK)
     */
    AlarmMethodType getMethodType();

    AlarmDeliveryPayload prepare(AlarmRuleV2 rule,
                                 AlarmNotificationChannel channel,
                                 AlarmNotificationChannelConfig channelConfig,
                                 MetricQueryResult metricResults);

    /**
     * Sends the persisted message. Implementations must not reload the rule, channel or
     * template here -- what the notification says was decided at enqueue time. Resolving
     * a destination the payload names to its current recipients is expected.
     */
    void send(AlarmNotificationOutbox delivery, AlarmDeliveryPayload payload);
}
