/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.test.pinpoint.plugin.rabbitmq.spring.service;

import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;
import com.navercorp.test.pinpoint.plugin.rabbitmq.MessageConverter;
import com.navercorp.test.pinpoint.plugin.rabbitmq.PropagationMarker;
import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.receiver.TestReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HyunGil Jeong
 */
@Service
public class TestReceiverService {

    private final TestReceiver testReceiver;
    private final PropagationMarker propagationMarker;

    @Autowired
    public TestReceiverService(TestReceiver testReceiver) {
        if (testReceiver == null) {
            throw new NullPointerException("testReceiver");
        }
        this.testReceiver = testReceiver;
        this.propagationMarker = new PropagationMarker();
    }

    public <T> T receiveMessage(MessageConverter<T> messageConverter) {
        T message = testReceiver.receiveMessage(RabbitMQTestConstants.QUEUE_PULL, messageConverter);
        if (message == null) {
            return null;
        }
        propagationMarker.mark();
        return message;
    }

    public <T> T receiveMessage(MessageConverter<T> messageConverter, long timeoutMs) throws InterruptedException {
        T message = testReceiver.receiveMessage(RabbitMQTestConstants.QUEUE_PULL, messageConverter, timeoutMs);
        if (message == null) {
            return null;
        }
        propagationMarker.mark();
        return message;
    }
}
