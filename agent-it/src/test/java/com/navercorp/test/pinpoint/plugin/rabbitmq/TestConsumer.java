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

package com.navercorp.test.pinpoint.plugin.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author HyunGil Jeong
 */
public class TestConsumer<T> extends DefaultConsumer {

    private final PropagationMarker marker = new PropagationMarker();

    private final MessageConverter<T> messageConverter;

    private final BlockingQueue<T> messages = new LinkedBlockingQueue<T>();

    public TestConsumer(Channel channel, MessageConverter<T> messageConverter) {
        super(channel);
        if (messageConverter == null) {
            throw new NullPointerException("messageConverter");
        }
        this.messageConverter = messageConverter;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        if (body == null) {
            return;
        }
        marker.mark();
        messages.add(messageConverter.convertMessage(body));
    }

    public T getMessage(long timeoutMs, TimeUnit unit) throws InterruptedException {
        return messages.poll(timeoutMs, unit);
    }
}
