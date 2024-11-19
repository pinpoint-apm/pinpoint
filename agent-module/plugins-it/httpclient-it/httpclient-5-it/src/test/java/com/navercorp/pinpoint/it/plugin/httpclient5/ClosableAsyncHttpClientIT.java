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

package com.navercorp.pinpoint.it.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

/**
 * @author netspider
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.httpcomponents.client5:httpclient5:[5.0,]", WebServer.VERSION})
public class ClosableAsyncHttpClientIT extends HttpClientITBase {

    @Test
    public void test() throws Exception {
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().useSystemProperties().build();
        httpClient.start();

        try {
            SimpleHttpRequest request = newGetRequest(getAddress());
            Future<SimpleHttpResponse> future = httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse simpleHttpResponse) {
                }

                @Override
                public void failed(Exception e) {
                }

                @Override
                public void cancelled() {
                }
            });
            future.get();
        } finally {
            httpClient.close();
        }

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printMethod();

        verifier.ignoreServiceType("HTTP_CLIENT_5");
    }

    @SuppressWarnings("deprecation")
    private SimpleHttpRequest newGetRequest(String uri) {
        return SimpleHttpRequests.get(uri);
    }
}