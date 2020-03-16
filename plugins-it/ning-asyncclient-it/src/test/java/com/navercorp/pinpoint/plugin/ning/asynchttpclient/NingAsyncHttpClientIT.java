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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.*;

import java.util.concurrent.Future;

import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointPluginTestSuite;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Request;
import com.ning.http.client.Response;

/**
 * @author netspider
 */
@RunWith(PinpointPluginTestSuite.class)
@PinpointAgent(AgentPath.PATH)
@Dependency({ "com.ning:async-http-client:[1.7.24],[1.8.16,1.8.999)",
        WebServer.VERSION, PluginITConstants.VERSION})
@JvmVersion(7)
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-ning-asynchttpclient-plugin"})
public class NingAsyncHttpClientIT {

    private static WebServer webServer;

    @BeforeClass
    public static void BeforeClass() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @AfterClass
    public static void AfterClass() {
        webServer = WebServer.cleanup(webServer);
    }

    @Test
    public void test() throws Exception {
        AsyncHttpClient client = new AsyncHttpClient();
        
        try {
            Future<Response> f = client.preparePost(webServer.getCallHttpUrl()).addParameter("param1", "value1").execute();
            Response response = f.get();
        } finally {
            client.close();
        }
        
        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        String destinationId = webServer.getHostAndPort();
        String httpUrl = webServer.getCallHttpUrl();
        verifier.verifyTrace(event("ASYNC_HTTP_CLIENT", AsyncHttpClient.class.getMethod("executeRequest", Request.class, AsyncHandler.class), null, null, destinationId,
                annotation("http.url", httpUrl)));
        verifier.verifyTraceCount(0);
   }
}