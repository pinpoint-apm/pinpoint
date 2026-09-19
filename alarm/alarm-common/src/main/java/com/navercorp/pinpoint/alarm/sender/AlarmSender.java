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
