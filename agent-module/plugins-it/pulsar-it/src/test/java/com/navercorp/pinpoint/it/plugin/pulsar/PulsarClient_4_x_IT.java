/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.pulsar;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import com.navercorp.pinpoint.test.plugin.shared.SharedDependency;
import com.navercorp.pinpoint.test.plugin.shared.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Test;
import test.pinpoint.plugin.pulsar.TestProducer;

/**
 * @author zhouzixin@apache.org
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-pulsar-client.config")
@Dependency({"org.apache.pulsar:pulsar-client:[4.0.0,4.max)"})
@SharedDependency({"org.apache.pulsar:pulsar-client:4.0.0",
        TestcontainersOption.TEST_CONTAINER, TestcontainersOption.PULSAR})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class PulsarClient_4_x_IT extends PulsarClientITBase {

    @Test
    public void testSend() throws Exception {
        try (TestProducer testProducer = new TestProducer()) {
            testProducer.sendMessage(serviceUrl);
            verifySend();
        }
    }
}