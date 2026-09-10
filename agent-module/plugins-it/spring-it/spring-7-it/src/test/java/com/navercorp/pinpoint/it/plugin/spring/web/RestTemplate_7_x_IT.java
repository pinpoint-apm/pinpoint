/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.it.plugin.spring.web;

import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifier;
import com.navercorp.pinpoint.bootstrap.plugin.test.PluginTestVerifierHolder;
import com.navercorp.pinpoint.common.util.StringStringValue;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.it.plugin.utils.HeaderRecordingWebServer;
import com.navercorp.pinpoint.it.plugin.utils.PluginITConstants;
import com.navercorp.pinpoint.it.plugin.utils.WebServer;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.JvmVersion;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PinpointConfig;
import com.navercorp.pinpoint.test.plugin.PluginForkedTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.web.client.RestTemplate;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * RestTemplate on Spring Framework 7. Covers the {@code RestTemplate(Iterable)} constructor introduced
 * in 7.0 and the response header adaptor, which only runs when response header recording is on
 * ({@code profiler.http.record.response.headers}) — on 7.x its former {@code containsKey} /
 * {@code get(Object)} / {@code keySet} calls raised {@code NoSuchMethodError}.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-web:[7.0.0,7.max]",
        WebServer.VERSION, PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-resttemplate-plugin"})
@PinpointConfig("pinpoint-resttemplate-7.config")
public class RestTemplate_7_x_IT {

    // package-private in spring-web, so it is named by descriptor
    private static final String SIMPLE_CLIENT_HTTP_RESPONSE =
            "org.springframework.http.client.SimpleClientHttpResponse.SimpleClientHttpResponse(java.net.HttpURLConnection)";

    private static HeaderRecordingWebServer webServer;

    @BeforeAll
    public static void beforeClass() throws Exception {
        // Its response carries X-Test-Echo / X-Test-Multi, which the response header adaptor must read back.
        webServer = HeaderRecordingWebServer.newTestWebServer();
    }

    @AfterAll
    public static void afterClass() {
        WebServer.cleanup(webServer);
    }

    @Test
    public void getForObject_recordsTheClientCall_andResponseHeaders() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String body = restTemplate.getForObject(webServer.getCallHttpUrl(), String.class);

        Assertions.assertNotNull(body);
        // The resttemplate plugin records the call only; propagation headers are injected by the plugin of the
        // underlying HTTP client (not imported here), so only the span events are asserted.

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // 7.0: RestTemplate() delegates to the new RestTemplate(Iterable) constructor, whose body runs first.
        verifier.verifyTrace(event("REST_TEMPLATE", RestTemplate.class.getConstructor(Iterable.class)));
        verifier.verifyTrace(event("REST_TEMPLATE", RestTemplate.class.getConstructor()));
        verifier.verifyTrace(event("REST_TEMPLATE", AbstractClientHttpRequest.class.getMethod("execute"),
                annotation("http.status.code", 200)));
        // The response interceptor runs the response header adaptor (getHeaders) for the two configured headers.
        verifier.verifyTrace(event("REST_TEMPLATE", SIMPLE_CLIENT_HTTP_RESPONSE,
                annotation("http.status.code", 200),
                annotation("http.resp.header", new StringStringValue("X-Test-Echo", "echo")),
                annotation("http.resp.header", new StringStringValue("X-Test-Multi", "a"))));
    }
}
