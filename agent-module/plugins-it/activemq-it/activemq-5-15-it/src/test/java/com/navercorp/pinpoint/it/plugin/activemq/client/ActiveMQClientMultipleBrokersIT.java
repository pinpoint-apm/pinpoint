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

package com.navercorp.pinpoint.it.plugin.activemq.client;

import com.navercorp.pinpoint.it.plugin.activemq.client.util.ActiveMQClientITHelper;
import com.navercorp.pinpoint.it.plugin.activemq.client.util.PortUtils;
import com.navercorp.pinpoint.it.plugin.activemq.client.util.TestBroker;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.testcase.util.SocketUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Arrays;

/**
 * @author HyunGil Jeong
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("activemq/client/pinpoint-activemq-client.config")
// 5.4.1 bug creates activemq-data directory even if persistence is set to false - skip it
// 5.5.x activemq-all missing slf4j binder - just skip instead of supplying one
@Dependency({"org.apache.activemq:activemq-all:[5.1.0,5.4.1),[5.4.2,5.4.max],[5.6.0,5.14.max]"})
public class ActiveMQClientMultipleBrokersIT extends ActiveMQClientITBase {

    private static final String PRODUCER_BROKER = "Producer_Broker";
    private static final String CONSUMER_BROKER = "Consumer_Broker";

    private static String PRODUCER_BROKER_URL;
    private static String CONSUMER_BROKER_URL;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        final int producerBrokerPort = SocketUtils.findAvailableTcpPort(10000, 19999);
        PRODUCER_BROKER_URL = PortUtils.toUrl(producerBrokerPort);
        final int consumerBrokerPort = SocketUtils.findAvailableTcpPort(20000, 29999);
        CONSUMER_BROKER_URL = PortUtils.toUrl(consumerBrokerPort);

        ActiveMQClientITHelper.startBrokers(Arrays.asList(
                // Consumer broker
                new TestBroker.TestBrokerBuilder(CONSUMER_BROKER)
                        .addConnector(CONSUMER_BROKER_URL)
                        .build(),
                // Producer broker - forwards to consumer broker
                new TestBroker.TestBrokerBuilder(PRODUCER_BROKER)
                        .addConnector(PRODUCER_BROKER_URL)
                        .addNetworkConnector("static:(" + CONSUMER_BROKER_URL + ")")
                        .build()
        ));
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
        ActiveMQClientITHelper.stopBrokers();
    }

    @Override
    protected String getProducerBrokerName() {
        return PRODUCER_BROKER;
    }

    @Override
    protected String getProducerBrokerUrl() {
        return PRODUCER_BROKER_URL;
    }

    @Override
    protected String getConsumerBrokerName() {
        return CONSUMER_BROKER;
    }

    @Override
    protected String getConsumerBrokerUrl() {
        return CONSUMER_BROKER_URL;
    }

}
