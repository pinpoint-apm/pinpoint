/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.it.http;

import com.navercorp.pinpoint.plugin.thrift.common.TestEnvironment;
import com.navercorp.pinpoint.plugin.thrift.common.client.HttpEchoTestClient;
import com.navercorp.pinpoint.plugin.thrift.common.server.HttpEchoTestServer;
import com.navercorp.pinpoint.plugin.thrift.it.EchoTestRunner;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import org.apache.thrift.transport.TTransportException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author HyunGil Jeong
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@JvmVersion(8)
@Dependency({ "org.apache.thrift:libthrift:[0.9.1,)", "org.eclipse.jetty:jetty-server:9.2.11.v20150529",
        "org.slf4j:slf4j-simple:1.6.6", "org.slf4j:log4j-over-slf4j:1.6.6", "org.slf4j:slf4j-api:1.6.6" })
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-thrift-plugin",
        "com.navercorp.pinpoint:pinpoint-jdk-http-plugin",
        "com.navercorp.pinpoint:pinpoint-jetty-plugin"
        })
public class ThriftHttpIT extends EchoTestRunner<HttpEchoTestServer> {

    @Override
    protected HttpEchoTestServer createEchoServer(TestEnvironment environment) {
        return HttpEchoTestServer.createServer(environment);
    }

    @Test
    public void testThriftHttpCall() throws Exception {
        // Given
        final String expectedMessage = "TEST_MESSAGE";
        // When
        final HttpEchoTestClient client = getServer().getHttpClient();
        final String result = invokeAndVerify(client, expectedMessage);
        // Then
        assertEquals(expectedMessage, result);
    }
}
