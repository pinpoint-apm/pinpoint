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
 * The client itself works against the test broker from 4.0.0-incubating on, but the plugin only traces the
 * synchronous send there. The cases it does not cover are disabled; the assertions are the same as for every
 * other range.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@PinpointConfig("pinpoint-rocketmq-client.config")
@Dependency({"org.apache.rocketmq:rocketmq-client:[4.0.0,4.1.0)"})
@SharedDependency({TestcontainersOption.TEST_CONTAINER})
@SharedTestLifeCycleClass(TestBrokerServer.class)
public class RocketMQClient_4_0_x_IT extends RocketMQClientITBase {

    /**
     * NettyRemotingClient.channelTables is declared as ConcurrentHashMap, not ConcurrentMap, so the getter cannot be
     * added and the consumer is left untraced.
     */
    @Test
    @Disabled("the push consumer is traced from 4.1.0")
    @Override
    public void syncSend_concurrentlyListener() throws Exception {
        super.syncSend_concurrentlyListener();
    }

    @Test
    @Disabled("the push consumer is traced from 4.1.0")
    @Override
    public void syncSend_orderlyListener() throws Exception {
        super.syncSend_orderlyListener();
    }

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
