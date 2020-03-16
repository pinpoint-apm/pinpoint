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

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Jiaqi Feng
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("rabbitmq/client/pinpoint-rabbitmq.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-rabbitmq-plugin", "com.navercorp.pinpoint:pinpoint-jetty-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
@Dependency({"com.rabbitmq:amqp-client:[5.0.0,)", "org.apache.qpid:qpid-broker:6.1.1"})
@JvmVersion(8)
public class RabbitMQClient_5_x_IT extends RabbitMQClientITBase {

    @Test
    public void testPush() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);

        testRunner.runPushTest();
    }

    @Test
    public void testPush_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);

        testRunner.runPushTest();
    }

    @Test
    public void testPush_nio() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.useNio();

        testRunner.runPushTest();
    }

    @Test
    public void testPush_nio_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.useNio();

        testRunner.runPushTest();
    }

    @Test
    public void testPull() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);

        testRunner.runPullTest();
    }

    @Test
    public void testPull_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);

        testRunner.runPullTest();
    }

    @Test
    public void testPull_nio() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.useNio();

        testRunner.runPullTest();
    }

    @Test
    public void testPull_nio_autorecovery() throws Exception {
        ConnectionFactory connectionFactory = getConnectionFactory();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.useNio();

        testRunner.runPullTest();
    }
}
