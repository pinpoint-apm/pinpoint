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

package com.navercorp.test.pinpoint.plugin.rabbitmq.spring.config;

import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.handler.TestMessageHandler;
import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author HyunGil Jeong
 */
@Configuration
@ComponentScan("com.navercorp.test.pinpoint.plugin.rabbitmq.spring.handler")
public class MessageListenerConfig_Pre_1_4_0 {

    @Bean
    public SimpleMessageListenerContainer listenerContainer(ConnectionFactory connectionFactory, TestMessageHandler testMessageHandler) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RabbitMQTestConstants.QUEUE_PUSH);
        container.setMessageListener(new MessageListenerAdapter(testMessageHandler));
        return container;
    }
}
