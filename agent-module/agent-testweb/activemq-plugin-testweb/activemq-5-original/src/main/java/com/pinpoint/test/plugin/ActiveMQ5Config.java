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

package com.pinpoint.test.plugin;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Connection;
import javax.jms.JMSException;

@Configuration
public class ActiveMQ5Config {

    /**
     * Broker inside the app. Set activemq.broker.embedded=false to use an external broker at activemq.broker.url.
     * The client still connects over tcp, the transport the plugin reads the endpoint from.
     */
    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(name = "activemq.broker.embedded", havingValue = "true", matchIfMissing = true)
    public BrokerService broker(@Value("${activemq.broker.url}") String brokerUrl) throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("activemq5-testweb");
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.addConnector(brokerUrl);
        broker.start();
        return broker;
    }

    @Bean(destroyMethod = "close")
    public Connection connection(@Value("${activemq.broker.url}") String brokerUrl,
                                 ObjectProvider<BrokerService> broker) throws JMSException {
        // start the embedded broker, when there is one, before connecting to it
        broker.getIfAvailable();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        return connection;
    }
}
