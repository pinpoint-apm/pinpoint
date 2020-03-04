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
import com.navercorp.pinpoint.plugin.rabbitmq.util.TestBroker;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */

public abstract class RabbitMQClientITBase {

    private static final TestBroker BROKER = new TestBroker();

    private final ConnectionFactory connectionFactory = new ConnectionFactory();
    protected final RabbitMQTestRunner testRunner = new RabbitMQTestRunner(connectionFactory);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        BROKER.start();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        BROKER.shutdown();
    }

    @Before
    public void setUp() {
        connectionFactory.setHost(RabbitMQTestConstants.BROKER_HOST);
        connectionFactory.setPort(RabbitMQTestConstants.BROKER_PORT);
        connectionFactory.setSaslConfig(RabbitMQTestConstants.SASL_CONFIG);
    }

    final ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
