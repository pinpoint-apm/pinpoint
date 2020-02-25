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

import com.navercorp.test.pinpoint.plugin.rabbitmq.spring.TestMessageHolder;
import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author HyunGil Jeong
 */
@Configuration
@EnableRabbit
@ComponentScan("com.navercorp.test.pinpoint.plugin.rabbitmq.spring.service")
public class CommonConfig {

    @Bean(name = "connectionFactory")
    public ConnectionFactory connectionFactory() {
        com.rabbitmq.client.ConnectionFactory connectionFactory = new com.rabbitmq.client.ConnectionFactory();
        connectionFactory.setHost(RabbitMQTestConstants.BROKER_HOST);
        connectionFactory.setPort(RabbitMQTestConstants.BROKER_PORT);
        connectionFactory.setSaslConfig(RabbitMQTestConstants.SASL_CONFIG);
        connectionFactory.setAutomaticRecoveryEnabled(false);
        return new CachingConnectionFactory(connectionFactory);
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean(name = "testExchange")
    public DirectExchange testExchange() {
        return new DirectExchange(RabbitMQTestConstants.EXCHANGE, false, false);
    }

    @Bean(name = "testPushQueue")
    public Queue testPushQueue() {
        return new Queue(RabbitMQTestConstants.QUEUE_PUSH, false, false, false);
    }

    @Bean(name = "testPullQueue")
    public Queue testPullQueue() {
        return new Queue(RabbitMQTestConstants.QUEUE_PULL, false, false, false);
    }

    @Bean(name = "pushBinding")
    public Binding pushBinding() {
        return BindingBuilder.bind(testPushQueue()).to(testExchange()).with(RabbitMQTestConstants.ROUTING_KEY_PUSH);
    }

    @Bean(name = "pullBinding")
    public Binding pullBinding() {
        return BindingBuilder.bind(testPullQueue()).to(testExchange()).with(RabbitMQTestConstants.ROUTING_KEY_PULL);
    }

    @Bean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean(name = "testMessageHolder")
    public TestMessageHolder testMessageHolder() {
        return new TestMessageHolder();
    }
}
