/*
 * Copyright 2020 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.pluginit.utils.AgentPath;
import com.navercorp.pinpoint.pluginit.utils.PluginITConstants;
import com.navercorp.pinpoint.pluginit.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.anyAnnotationValue;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * @author jaehong.kim
 */
@PinpointAgent(AgentPath.PATH)
@ImportPlugin("com.navercorp.pinpoint:pinpoint-httpclient4-plugin")
@Dependency({"org.apache.httpcomponents:httpclient:[4.3],[4.3.1],[4.3.2],[4.3.3],[4.3.4],[4.3.6],[4.4],[4.4.1],[4.5],[4.5.1],[4.5.2],[4.5.3],[4.5.4],[4.3.5]",
        WebServer.VERSION, PluginITConstants.VERSION})
public class CloaeableHttpClientIT extends HttpClientITBase {

    @Test
    public void test() throws Exception {
        HttpGet httpget = new HttpGet(getAddress());

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(httpget)) {

            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();

        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", CloseableHttpClient.class.getMethod("execute", HttpUriRequest.class)));
        final String display = getHostPort();
        Method connect = PoolingHttpClientConnectionManager.class.getMethod("connect", HttpClientConnection.class, HttpRoute.class, int.class, HttpContext.class);
        verifier.verifyTrace(event("HTTP_CLIENT_4_INTERNAL", connect,
                annotation("http.internal.display", display)
        ));

        final String destinationId = getHostPort();
        verifier.verifyTrace(event("HTTP_CLIENT_4",
                HttpRequestExecutor.class.getMethod("execute", HttpRequest.class, HttpClientConnection.class, HttpContext.class), null, null, destinationId,
                annotation("http.url", "/"),
                annotation("http.status.code", 200),
                annotation("http.io", anyAnnotationValue())
        ));

        verifier.verifyTraceCount(0);
    }
}
