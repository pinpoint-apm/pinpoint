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

package com.navercorp.pinpoint.it.plugin.httpclient5;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient5.HttpClient5RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient5.HttpRequest5ClientHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.http.message.BasicHttpRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Calls the httpclient5 plugin's request wrapper, header adaptor and extractors directly against real
 * HttpClient 5.x objects of every release in range - no interceptor, no HTTP call. Under {@code @PluginTest}
 * the plugin classes are linked against the httpclient5/httpcore5 of the current case.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.httpcomponents.client5:httpclient5:[5.0,]"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-httpclient5-plugin"})
public class HttpClient5Adaptors_IT {

    private final ClientHeaderAdaptor<HttpRequest> headerAdaptor = new HttpRequest5ClientHeaderAdaptor();

    @Test
    public void requestWrapper_destinationAndUrl() {
        HttpRequest request = new BasicHttpRequest("GET", "/path?q=1");

        ClientRequestWrapper wrapper = new HttpClient5RequestWrapper(request, "host.example:8080");
        Assertions.assertEquals("host.example:8080", wrapper.getDestinationId(), "destination is the host the interceptor resolved");
        Assertions.assertEquals("/path?q=1", wrapper.getUrl(), "url is the request uri as sent");

        Assertions.assertNull(new HttpClient5RequestWrapper(request, null).getDestinationId(), "null host passes through (recorder applies the default)");
    }

    @Test
    public void headerAdaptor_setGetContains() {
        HttpRequest request = new BasicHttpRequest("GET", "/");

        Assertions.assertFalse(headerAdaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertEquals("", headerAdaptor.getHeader(request, "Pinpoint-TraceID"), "absent header reads as empty string");

        headerAdaptor.setHeader(request, "Pinpoint-TraceID", "agent^1^1");
        headerAdaptor.setHeader(request, "Pinpoint-TraceID", "agent^1^2");

        Assertions.assertTrue(headerAdaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertTrue(headerAdaptor.contains(request, "pinpoint-traceid"), "header names are case-insensitive");
        Assertions.assertEquals("agent^1^2", headerAdaptor.getHeader(request, "Pinpoint-TraceID"), "set replaces");
        Assertions.assertEquals(1, request.getHeaders("Pinpoint-TraceID").length, "set must not accumulate values");
    }

    @Test
    public void cookieAndEntityExtractors() {
        HttpRequest get = new BasicHttpRequest("GET", "/");
        Assertions.assertNull(HttpClient5CookieExtractor.INSTANCE.getCookie(get), "no Cookie header -> null");
        get.addHeader("Cookie", "a=1; b=2");
        Assertions.assertEquals("a=1; b=2", HttpClient5CookieExtractor.INSTANCE.getCookie(get));

        Assertions.assertNull(HttpClient5EntityExtractor.INSTANCE.getEntity(get), "a plain request carries no entity");
        BasicClassicHttpRequest post = new BasicClassicHttpRequest("POST", "/");
        post.setEntity(new StringEntity("hello"));
        Assertions.assertEquals("HTTP entity length: 5", HttpClient5EntityExtractor.INSTANCE.getEntity(post), "5.x records the length, not the body");
    }
}
