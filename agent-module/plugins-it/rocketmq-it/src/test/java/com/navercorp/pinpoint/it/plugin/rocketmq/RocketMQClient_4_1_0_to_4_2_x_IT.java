/*
 * Copyright 2026 NAVER Corp.
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
package com.navercorp.pinpoint.it.plugin.rocketmq;

import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.TestcontainersOption;
import com.navercorp.pinpoint.test.plugin.api.Dependency;
import com.navercorp.pinpoint.test.plugin.api.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.api.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.api.PluginTest;
import com.navercorp.pinpoint.test.plugin.api.SharedDependency;
import com.navercorp.pinpoint.test.plugin.api.SharedTestLifeCycleClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The plugin traces the synchronous send and the push consumer on these versions, but not the asynchronous send.
 * That case is disabled; the assertions are the same as for every other range.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-rocketmq-client.config")
@Dependency({"org.apache.rocketmq:rocketmq-client:[4.1.0,4.3.0)"})
@SharedDependency({TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class RocketMQClient_4_1_0_to_4_2_x_IT extends RocketMQClientITBase {

    /**
     * The async send runs on the calling thread, where ProducerSendInterceptor skips it because a trace is already
     * active: it is neither traced nor propagated.
     */
    @Test
    @Disabled("the asynchronous send is traced and propagated from 4.3.0")
    @Override
    public void asyncSend_sendCallback() throws Exception {
        super.asyncSend_sendCallback();
    }
}
