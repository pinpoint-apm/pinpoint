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

package com.navercorp.pinpoint.plugin.activemq.client;

import com.navercorp.pinpoint.plugin.activemq.client.util.ActiveMQClientITHelper;
import com.navercorp.pinpoint.plugin.activemq.client.util.TestBroker;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.util.Arrays;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("activemq/client/pinpoint-activemq-client.config")
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-activemq-client-plugin", "com.navercorp.pinpoint:pinpoint-user-plugin"})
// 5.4.1 bug creates activemq-data directory even if persistence is set to false - skip it
// 5.5.x activemq-all missing slf4j binder - just skip instead of supplying one
@Dependency({"org.apache.activemq:activemq-all:[5.1.0,5.4.1),[5.4.2,5.4.max],[5.6.0,5.14.max]"})
public class ActiveMQClientMultipleBrokersIT extends ActiveMQClientITBase {

    private static final String PRODUCER_BROKER = "Producer_Broker";
    private static final String CONSUMER_BROKER = "Consumer_Broker";

    private static final String PRODUCER_BROKER_URL = "tcp://127.0.0.1:61616";
    private static final String CONSUMER_BROKER_URL = "tcp://127.0.0.1:61617";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
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

    @AfterClass
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
