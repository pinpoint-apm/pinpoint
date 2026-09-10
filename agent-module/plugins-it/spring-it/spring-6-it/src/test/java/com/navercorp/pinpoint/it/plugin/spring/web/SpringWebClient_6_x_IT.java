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
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.annotation;
import static com.navercorp.pinpoint.bootstrap.plugin.test.Expectations.event;

/**
 * WebClient on Spring Framework 6 with the spring-webflux client instrumentation enabled — the
 * regression guard for the header adaptor change made for Spring 7 (see the 7.x IT).
 *
 * <p>Spring 7.0 dropped the {@code MultiValueMap} implementation from {@code HttpHeaders}. The plugin's
 * header adaptor used to call the {@code Map} methods it was compiled against on Spring 5.3
 * ({@code containsKey}, {@code put}, {@code keySet}, {@code get(Object)}), which no longer exist on 7.x:
 * {@code BodyInserterRequestBuilderWriteToInterceptor.checkBeforeTraceBlockBegin} threw
 * {@code NoSuchMethodError} on every request, the SPRING_WEBFLUX_CLIENT span event was never recorded
 * and its header injection was skipped (the reactor-netty plugin underneath kept the trace alive).
 *
 * <p>This IT pins both halves: the propagation headers reach the wire, and the WebFlux client event
 * exists. Without the adaptor fix the second assertion fails on 7.x.
 */
@PluginForkedTest
@PinpointAgent(AgentPath.PATH)
@JvmVersion(17)
@Dependency({"org.springframework:spring-webflux:[6.0.0,6.max]",
        // same version as spring-webflux: ReactorClientHttpConnector implements SmartLifecycle (spring-context, optional for webflux)
        "org.springframework:spring-context",
        "io.projectreactor.netty:reactor-netty-http:1.2.18",
        WebServer.VERSION, PluginITConstants.VERSION})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-webflux-plugin",
        "com.navercorp.pinpoint:pinpoint-reactor-netty-plugin",
        "com.navercorp.pinpoint:pinpoint-netty-plugin",
        "com.navercorp.pinpoint:pinpoint-reactor-plugin"})
@PinpointConfig("pinpoint-webflux-client.config")
public class SpringWebClient_6_x_IT {

    // The span event the adaptor fix restores on Spring 7 (absent without it: checkBeforeTraceBlockBegin
    // threw before the event began).
    private static final String WRITE_TO = "org.springframework.web.reactive.function.client.DefaultClientRequestBuilder$BodyInserterRequest"
            + ".writeTo(org.springframework.http.client.reactive.ClientHttpRequest, org.springframework.web.reactive.function.client.ExchangeStrategies)";

    private static HeaderRecordingWebServer webServer;

    @BeforeAll
    public static void beforeClass() throws Exception {
        webServer = HeaderRecordingWebServer.newTestWebServer();
    }

    @AfterAll
    public static void afterClass() {
        WebServer.cleanup(webServer);
    }

    @Test
    public void propagationHeadersReachTheServer_andTheWebFluxClientEventIsRecorded() throws Exception {
        webServer.clear();
        WebClient client = WebClient.create(webServer.getCallHttpUrl());

        String body = client.get().uri("/").retrieve().bodyToMono(String.class).block(Duration.ofSeconds(10));

        Assertions.assertNotNull(body);
        Map<String, String> received = webServer.getLastRequestHeaders();
        Assertions.assertTrue(received.containsKey("Pinpoint-TraceID"), "Pinpoint-TraceID not propagated: " + received.keySet());
        Assertions.assertTrue(received.containsKey("Pinpoint-SpanID"), "Pinpoint-SpanID not propagated: " + received.keySet());
        Assertions.assertTrue(received.containsKey("Pinpoint-pAppName"), "Pinpoint-pAppName not propagated: " + received.keySet());

        PluginTestVerifier verifier = PluginTestVerifierHolder.getInstance();
        verifier.printCache();
        // Recorded in the reactor-netty async chunk: nextSpanId + destination + http.url come from the request trace writer.
        verifier.verifyDiscreteTrace(event("SPRING_WEBFLUX_CLIENT", WRITE_TO, null, null, webServer.getHostAndPort(),
                annotation("http.url", webServer.getCallHttpUrl() + "/")));
    }
}
