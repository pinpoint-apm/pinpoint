/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.pinpoint.plugin.rabbitmq.spring.listener;

import com.navercorp.pinpoint.it.plugin.rabbitmq.util.RabbitMQTestConstants;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import test.pinpoint.plugin.rabbitmq.PropagationMarker;
import test.pinpoint.plugin.rabbitmq.spring.TestMessageHolder;
import test.pinpoint.plugin.rabbitmq.spring.config.MessageListenerConfig;

import java.util.Objects;

@Component
public class TestMessageListener {

    private final PropagationMarker marker = new PropagationMarker();
    private final TestMessageHolder testMessageHolder;

    @Autowired
    public TestMessageListener(TestMessageHolder testMessageHolder) {
        this.testMessageHolder = Objects.requireNonNull(testMessageHolder, "testMessageHolder");
    }

    @RabbitListener(queues = RabbitMQTestConstants.QUEUE_PUSH, containerFactory = MessageListenerConfig.SIMPLE_CONTAINER_FACTORY)
    public void receiveMessage(String message) {
        marker.mark();
        testMessageHolder.addMessage(message);
    }

    @RabbitListener(queues = RabbitMQTestConstants.QUEUE_DIRECT_PUSH, containerFactory = MessageListenerConfig.DIRECT_CONTAINER_FACTORY)
    public void receiveDirectMessage(String message) {
        marker.mark();
        testMessageHolder.addMessage(message);
    }
}
