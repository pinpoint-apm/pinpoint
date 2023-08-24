/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.it.synchronous;

import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.EchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.server.SyncEchoTestServer014;
import com.navercorp.pinpoint.plugin.thrift.common.server.ThriftEchoTestServer;
import com.navercorp.pinpoint.plugin.thrift.it.EchoTestRunner;
import com.navercorp.pinpoint.plugin.thrift.it.ThriftVersion;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TTransportException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * <p>Integration test for TThreadedSelectorServer with synchronous processor.</p>
 *
 * <p><strike>Tests against libthrift 0.9.2+ due to THRIFT-2274</strike>
 * Tests against libthrift 0.10.0+ (0.9.x -> 0.10.x introduces breaking change to generated thrift code)</p>
 * 
 * @author HyunGil Jeong
 */
@PinpointAgent(AgentPath.PATH)
@Dependency({ ThriftVersion.VERSION_14_16,
        "org.slf4j:slf4j-simple:1.6.6", "org.slf4j:log4j-over-slf4j:1.6.6", "org.slf4j:slf4j-api:1.6.6" })
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-thrift-plugin"})
public class ThriftThreadedSelectorServerIT014 extends EchoTestRunner<ThriftEchoTestServer<TThreadedSelectorServer>> {

    @Override
    protected ThriftEchoTestServer<TThreadedSelectorServer> createEchoServer(TestEnvironment environment)
            throws TTransportException {
        return SyncEchoTestServer014.SyncEchoTestServerFactory.threadedSelectorServer(environment);
    }

    @Test
    public void testSynchronousRpcCall() throws Exception {
        // Given
        final String expectedMessage = "TEST_MESSAGE";
        // When
        final EchoTestClient client = getServer().getSynchronousClient();
        final String result = invokeAndVerify(client, expectedMessage);
        // Then
        Assertions.assertEquals(expectedMessage, result);
    }

    @Test
    public void testAsynchronousRpcCall() throws Exception {
        // Given
        final String expectedMessage = "TEST_MESSAGE";
        // When
        final EchoTestClient client = getServer().getAsynchronousClient();
        final String result = invokeAndVerify(client, expectedMessage);
        // Then
        Assertions.assertEquals(expectedMessage, result);
    }

}
