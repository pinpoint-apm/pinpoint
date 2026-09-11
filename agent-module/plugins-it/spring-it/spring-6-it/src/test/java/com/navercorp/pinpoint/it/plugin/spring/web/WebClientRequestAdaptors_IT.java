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

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.ClientHttpRequestCookieExtractor;
import com.navercorp.pinpoint.plugin.spring.webflux.interceptor.WebClientRequestWrapper;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.mock.http.client.reactive.MockClientHttpRequest;

import java.net.URI;

/**
 * Calls the spring-webflux plugin's WebClient request wrapper and cookie extractor directly against a real
 * reactive {@code ClientHttpRequest} of every Spring Framework 6 release in range - no interceptor,
 * no HTTP call. These feed destinationId / http.url and the cookie annotation of the SPRING_WEBFLUX_CLIENT event.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.springframework:spring-webflux:[6.0.0,6.max]",
        // same version as spring-webflux: reactive MockClientHttpRequest
        "org.springframework:spring-test"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-spring-webflux-plugin"})
public class WebClientRequestAdaptors_IT {

    private final CookieExtractor<ClientHttpRequest> cookieExtractor = new ClientHttpRequestCookieExtractor();

    @Test
    public void requestWrapper_destinationAndUrl() {
        ClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://host.example:8080/path?q=1"));

        ClientRequestWrapper wrapper = new WebClientRequestWrapper(request);
        Assertions.assertEquals("host.example:8080", wrapper.getDestinationId());
        Assertions.assertEquals("http://host.example:8080/path?q=1", wrapper.getUrl(), "url is the full request uri");

        ClientRequestWrapper noPort = new WebClientRequestWrapper(new MockClientHttpRequest(HttpMethod.GET, URI.create("https://host.example/path")));
        Assertions.assertEquals("host.example", noPort.getDestinationId(), "no explicit port -> host only");

        Assertions.assertEquals("Unknown", new WebClientRequestWrapper(null).getDestinationId(), "null request -> Unknown");
        Assertions.assertNull(new WebClientRequestWrapper(null).getUrl());
    }

    @Test
    public void cookieExtractor_nameEqualsValue_repeatsJoinedByComma() {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://host.example/"));
        Assertions.assertEquals("", cookieExtractor.getCookie(request), "no cookies -> empty string");

        request.getCookies().add("session", new HttpCookie("session", "abc"));
        Assertions.assertEquals("session=abc", cookieExtractor.getCookie(request));

        request.getCookies().add("session", new HttpCookie("session", "def"));
        Assertions.assertEquals("session=abc,session=def", cookieExtractor.getCookie(request), "repeated values of one cookie are comma-joined");
    }
}
