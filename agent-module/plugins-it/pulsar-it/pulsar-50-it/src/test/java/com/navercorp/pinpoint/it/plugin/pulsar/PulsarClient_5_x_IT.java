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
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginForkedTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;

/**
 * The shaded pulsar-client: the implementation is relocated to org.apache.pulsar.shade.org.apache.pulsar.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-pulsar-client.config")
// The shaded TopicName of 5.0.x still refers to org.apache.pulsar.common.naming.TopicDomain of pulsar-client-admin-api,
// which pulsar-client 5.0.x does not declare as a dependency (pulsar-client-all does).
@Dependency({"org.apache.pulsar:pulsar-client:[5.0.0,5.max]", "org.apache.pulsar:pulsar-client-admin-api"})
@SharedDependency({
        TestcontainersOption.TEST_CONTAINER,
        TestcontainersOption.PULSAR,
})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class PulsarClient_5_x_IT extends PulsarClientITBase {

    @Override
    String implPackage() {
        return "org.apache.pulsar.shade.org.apache.pulsar.client.impl";
    }
}
