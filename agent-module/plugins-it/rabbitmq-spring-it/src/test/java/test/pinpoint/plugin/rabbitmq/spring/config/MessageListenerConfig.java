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

package test.pinpoint.plugin.rabbitmq.spring.config;

import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("test.pinpoint.plugin.rabbitmq.spring.listener")
public class MessageListenerConfig {

    public static final String SIMPLE_CONTAINER_FACTORY = "rabbitListenerContainerFactory";
    public static final String DIRECT_CONTAINER_FACTORY = "directRabbitListenerContainerFactory";

    // SimpleMessageListenerContainer
    @Bean(name = SIMPLE_CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        return containerFactory;
    }

    // DirectMessageListenerContainer
    @Bean(name = DIRECT_CONTAINER_FACTORY)
    public DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        DirectRabbitListenerContainerFactory containerFactory = new DirectRabbitListenerContainerFactory();
        containerFactory.setConnectionFactory(connectionFactory);
        return containerFactory;
    }
}
