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

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ClientHttpRequestClientHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;

import java.net.URI;

/**
 * Calls the spring-webflux plugin's {@link ClientHttpRequestClientHeaderAdaptor} - the adaptor the WebClient
 * request trace writer uses to inject and read the Pinpoint propagation headers - directly against a real
 * reactive {@code ClientHttpRequest} of every Spring Framework 6 release in range. Issue #14276 was
 * this adaptor's {@code containsKey(Object)} throwing {@code NoSuchMethodError} on Spring 7; here the plugin
 * class is linked against the spring-web of the current case, so such a drift fails per version with the
 * offending method in the report.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-webflux:[6.0.0,6.max]",
        // same version as spring-webflux: reactive MockClientHttpRequest
        "org.springframework:spring-test"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-webflux-plugin"})
public class ClientHttpRequestClientHeaderAdaptor_IT {

    private final ClientHeaderAdaptor<ClientHttpRequest> adaptor = new ClientHttpRequestClientHeaderAdaptor();

    @Test
    public void setThenReadBack_andContains() {
        ClientHttpRequest request = request();

        Assertions.assertFalse(adaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertEquals("", adaptor.getHeader(request, "Pinpoint-TraceID"), "absent header reads as empty string");

        adaptor.setHeader(request, "Pinpoint-TraceID", "agent^1^1");
        adaptor.setHeader(request, "Pinpoint-SpanID", "42");

        Assertions.assertTrue(adaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertTrue(adaptor.contains(request, "pinpoint-traceid"), "header names are case-insensitive");
        Assertions.assertEquals("agent^1^1", adaptor.getHeader(request, "Pinpoint-TraceID"));
        Assertions.assertEquals("42", adaptor.getHeader(request, "Pinpoint-SpanID"));
        Assertions.assertEquals("agent^1^1", request.getHeaders().getFirst("Pinpoint-TraceID"), "value reached the real request");
    }

    @Test
    public void setReplacesThePreviousValue() {
        ClientHttpRequest request = request();

        adaptor.setHeader(request, "Pinpoint-Flags", "0");
        adaptor.setHeader(request, "Pinpoint-Flags", "1");

        Assertions.assertEquals("1", adaptor.getHeader(request, "Pinpoint-Flags"));
        Assertions.assertEquals(1, request.getHeaders().get("Pinpoint-Flags").size(), "set() must not accumulate values");
    }

    private static ClientHttpRequest request() {
        return new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost:8080/"));
    }
}
