/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.plugin.activemq.client.util.ActiveMQClientITHelper;
import com.navercorp.pinpoint.plugin.activemq.client.util.PortUtils;
import com.navercorp.pinpoint.plugin.activemq.client.util.TestBroker;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Collections;

/**
 * @author HyunGil Jeong
 */
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("activemq/client/pinpoint-activemq-client.config")
@JvmVersion(11)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-activemq-client-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
// 5.4.1 bug creates activemq-data directory even if persistence is set to false - skip it
// 5.5.x activemq-all missing slf4j binder - just skip instead of supplying one
@Dependency({"org.apache.activemq:activemq-all:[5.17.0,)"})
public class ActiveMQClientSingleBroker_5_17_x_IT extends ActiveMQClientITBase {

    private static final String BROKER_NAME = "Test_Broker";
    private static String BROKER_URL;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        final int brokerPort = SocketUtils.findAvailableTcpPort(10000, 19999);
        BROKER_URL = PortUtils.toUrl(brokerPort);

        ActiveMQClientITHelper.startBrokers(Collections.singletonList(
                new TestBroker.TestBrokerBuilder(BROKER_NAME)
                        .addConnector(BROKER_URL)
                        .build()
        ));
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        ActiveMQClientITHelper.stopBrokers();
    }

    @Override
    protected String getProducerBrokerName() {
        return BROKER_NAME;
    }

    @Override
    protected String getProducerBrokerUrl() {
        return BROKER_URL;
    }

    @Override
    protected String getConsumerBrokerName() {
        return BROKER_NAME;
    }

    @Override
    protected String getConsumerBrokerUrl() {
        return BROKER_URL;
    }

}
