/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq.client;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor.ConsumerHandleDeliveryInterceptor;

import java.lang.reflect.Modifier;

/**
 * @author Woonduk Kang(emeroad)
 */
public final class RabbitMQUtils {
    private RabbitMQUtils() {
    }

    public static boolean addConsumerHandleDeliveryInterceptor(InstrumentClass target) throws InstrumentException {
        if (target == null) {
            return false;
        }
        final InstrumentMethod handleDelivery = target.getDeclaredMethod("handleDelivery", "java.lang.String", "com.rabbitmq.client.Envelope", "com.rabbitmq.client.AMQP$BasicProperties", "byte[]");
        if (handleDelivery == null) {
            return false;
        }
        handleDelivery.addScopedInterceptor(ConsumerHandleDeliveryInterceptor.class, RabbitMQClientConstants.RABBITMQ_CONSUMER_SCOPE);
        return true;
    }

    public static MethodFilter getPublicApiFilter() {
        // RabbitTemplate
        // public APIs
        final MethodFilter publicApiFilter = MethodFilters.chain(
                MethodFilters.name("execute", "convertAndSend", "convertSendAndReceive", "convertSendAndReceiveAsType",
                        "correlationConvertAndSend", "doSend", "send", "sendAndReceive",
                        "receive", "receiveAndConvert", "receiveAndReply"),
                MethodFilters.modifier(Modifier.PUBLIC));
        return publicApiFilter;
    }

}
