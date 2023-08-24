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

package com.navercorp.pinpoint.plugin.rabbitmq;

import com.navercorp.pinpoint.plugin.rabbitmq.util.RabbitMQTestConstants;

import com.navercorp.pinpoint.test.plugin.shared.SharedTestBeforeAllResult;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.jupiter.api.BeforeEach;

import java.util.Properties;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
public abstract class RabbitMQClientITBase {

    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    protected final RabbitMQTestRunner testRunner = new RabbitMQTestRunner(connectionFactory);

    private static int port;

    @SharedTestBeforeAllResult
    public static void setBeforeAllResult(Properties beforeAllResult) {
        port = Integer.parseInt(beforeAllResult.getProperty("PORT"));
    }

    @BeforeEach
    public void setUp() {
        connectionFactory.setHost(RabbitMQTestConstants.BROKER_HOST);
        connectionFactory.setPort(port);
//        connectionFactory.setSaslConfig(RabbitMQTestConstants.SASL_CONFIG);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
    }

    final ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
