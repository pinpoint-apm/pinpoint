/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.it.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.DefaultHttpClientConnectionOperator;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.io.ConnectionEndpoint;
import org.apache.hc.client5.http.io.ManagedHttpClientConnection;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.net.NamedEndpoint;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author jaehong.kim
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.httpcomponents.client5:httpclient5:[5.6,]", WebServer.VERSION})
public class CloaeableHttpClient_5_6_IT extends HttpClientITBase {

    @Test
    public void test() throws Exception {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(getAddress());
            httpclient.execute(httpget, classicHttpResponse -> {
                return EntityUtils.toString(classicHttpResponse.getEntity());
            });
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.ignoreServiceType("HTTP_CLIENT_5");
        Method connect1 = PoolingHttpClientConnectionManager.class.getMethod("connect", ConnectionEndpoint.class, TimeValue.class, HttpContext.class);
        verifier.verifyTrace(event("HTTP_CLIENT_5_INTERNAL", connect1));
        Method connect2 = DefaultHttpClientConnectionOperator.class.getMethod("connect", ManagedHttpClientConnection.class, HttpHost.class, NamedEndpoint.class, Path.class, InetSocketAddress.class, Timeout.class, SocketConfig.class, Object.class, HttpContext.class);
        verifier.verifyTrace(event("HTTP_CLIENT_5_INTERNAL", connect2, annotation("http.internal.display", getAddress())));

        verifier.verifyTraceCount(0);
    }
}
