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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;

import java.io.IOException;

/**
 * @author HyunGil Jeong
 */
public class TestMessagePuller {

    private final Channel channel;
    private final PropagationMarker propagationMarker = new PropagationMarker();

    public TestMessagePuller(Channel channel) {
        if (channel == null) {
            throw new NullPointerException("channel");
        }
        this.channel = channel;
    }

    public <T> T pullMessage(MessageConverter<T> messageConverter, String queueName, boolean autoAck) throws IOException {
        GetResponse response = channel.basicGet(queueName, autoAck);
        if (response == null) {
            return null;
        }
        propagationMarker.mark();
        byte[] body = response.getBody();
        T message = messageConverter.convertMessage(body);
        if (!autoAck) {
            channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
        }
        return message;
    }

}
