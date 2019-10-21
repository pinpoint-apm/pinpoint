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

package com.navercorp.test.pinpoint.plugin.rabbitmq.spring.receiver;

import com.navercorp.test.pinpoint.plugin.rabbitmq.MessageConverter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * @author HyunGil Jeong
 */
public class TestReceiver_Post_1_6_0 implements TestReceiver {

    private final RabbitTemplate rabbitTemplate;

    public TestReceiver_Post_1_6_0(RabbitTemplate rabbitTemplate) {
        if (rabbitTemplate == null) {
            throw new NullPointerException("rabbitTemplate");
        }
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public <T> T receiveMessage(String queueName, MessageConverter<T> messageConverter) {
        Message message = rabbitTemplate.receive(queueName);
        return convertMessage(messageConverter, message);
    }

    @Override
    public <T> T receiveMessage(String queueName, MessageConverter<T> messageConverter, long timeoutMs) throws InterruptedException {
        Message message = rabbitTemplate.receive(queueName, timeoutMs);
        return convertMessage(messageConverter, message);
    }

    private <T> T convertMessage(MessageConverter<T> messageConverter, Message message) {
        if (message == null) {
            return null;
        }
        byte[] body = message.getBody();
        return messageConverter.convertMessage(body);
    }
}
