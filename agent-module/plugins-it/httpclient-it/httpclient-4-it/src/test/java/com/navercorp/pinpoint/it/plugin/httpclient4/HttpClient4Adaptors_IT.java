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

package com.navercorp.pinpoint.it.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseAdaptor;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient4.HttpRequest4ClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.httpclient4.HttpResponse4ClientHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * Calls the httpclient4 plugin's request wrapper, header adaptors and extractors directly against real
 * HttpClient 4.x objects of every release in range - no interceptor, no HTTP call. Under {@code @PluginTest}
 * the plugin classes are linked against the httpclient/httpcore of the current case, so both a dropped
 * descriptor and a changed value format show up per version.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"org.apache.httpcomponents:httpclient:[4.0,4.5.max]"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-httpclient4-plugin"})
public class HttpClient4Adaptors_IT {

    private static final ProtocolVersion HTTP_1_1 = new ProtocolVersion("HTTP", 1, 1);

    private final ClientHeaderAdaptor<HttpRequest> headerAdaptor = new HttpRequest4ClientHeaderAdaptor();
    private final ResponseAdaptor<HttpResponse> responseAdaptor = new HttpResponse4ClientHeaderAdaptor();

    @Test
    public void requestWrapper_destinationAndUrl() {
        HttpGet get = new HttpGet("/path?q=1");

        ClientRequestWrapper wrapper = new HttpClient4RequestWrapper(get, "host.example", 8080);
        Assertions.assertEquals("host.example:8080", wrapper.getDestinationId());
        Assertions.assertEquals("/path?q=1", wrapper.getUrl(), "url is the request line uri as sent");

        Assertions.assertEquals("host.example", new HttpClient4RequestWrapper(get, "host.example", -1).getDestinationId(), "no port -> host only");
        Assertions.assertEquals("UNKNOWN", new HttpClient4RequestWrapper(get, null, 8080).getDestinationId(), "null host -> UNKNOWN");

        HttpGet withHost = new HttpGet("/path");
        withHost.setHeader("Host", "header.example:9090");
        Assertions.assertEquals("header.example:9090", new HttpClient4RequestWrapper(withHost, "", -1).getDestinationId(), "blank host falls back to the Host header");
    }

    @Test
    public void headerAdaptor_setGetContains() {
        HttpGet get = new HttpGet("/");

        Assertions.assertFalse(headerAdaptor.contains(get, "Pinpoint-TraceID"));
        Assertions.assertEquals("", headerAdaptor.getHeader(get, "Pinpoint-TraceID"), "absent header reads as empty string");

        headerAdaptor.setHeader(get, "Pinpoint-TraceID", "agent^1^1");
        headerAdaptor.setHeader(get, "Pinpoint-TraceID", "agent^1^2");

        Assertions.assertTrue(headerAdaptor.contains(get, "Pinpoint-TraceID"));
        Assertions.assertTrue(headerAdaptor.contains(get, "pinpoint-traceid"), "header names are case-insensitive");
        Assertions.assertEquals("agent^1^2", headerAdaptor.getHeader(get, "Pinpoint-TraceID"), "set replaces");
        Assertions.assertEquals(1, get.getHeaders("Pinpoint-TraceID").length, "set must not accumulate values");
    }

    @Test
    public void responseAdaptor_readsAndWritesHeaders() {
        HttpResponse response = new BasicHttpResponse(HTTP_1_1, 200, "OK");
        response.addHeader("X-Test-Echo", "echo");
        response.addHeader("X-Test-Multi", "one");
        response.addHeader("X-Test-Multi", "two");

        Assertions.assertTrue(responseAdaptor.containsHeader(response, "X-Test-Echo"));
        Assertions.assertTrue(responseAdaptor.containsHeader(response, "x-test-echo"), "header names are case-insensitive");
        Assertions.assertFalse(responseAdaptor.containsHeader(response, "X-Missing"));

        Assertions.assertEquals("echo", responseAdaptor.getHeader(response, "X-Test-Echo"));
        Assertions.assertEquals("one", responseAdaptor.getHeader(response, "X-Test-Multi"), "first value wins");
        Assertions.assertNull(responseAdaptor.getHeader(response, "X-Missing"));

        // multi-valued lookups come back as a Set: order is not kept and duplicates collapse
        Assertions.assertEquals(new HashSet<String>(Arrays.asList("one", "two")), new HashSet<String>(responseAdaptor.getHeaders(response, "X-Test-Multi")));
        Assertions.assertTrue(responseAdaptor.getHeaders(response, "X-Missing").isEmpty());

        Collection<String> names = responseAdaptor.getHeaderNames(response);
        Assertions.assertEquals(new HashSet<String>(Arrays.asList("X-Test-Echo", "X-Test-Multi")), new HashSet<String>(names), "distinct names: " + names);

        responseAdaptor.setHeader(response, "X-Set", "first");
        responseAdaptor.setHeader(response, "X-Set", "second");
        Assertions.assertEquals(Arrays.asList("second"), new ArrayList<String>(responseAdaptor.getHeaders(response, "X-Set")));

        responseAdaptor.addHeader(response, "X-Add", "first");
        responseAdaptor.addHeader(response, "X-Add", "second");
        Assertions.assertEquals(new HashSet<String>(Arrays.asList("first", "second")), new HashSet<String>(responseAdaptor.getHeaders(response, "X-Add")));
    }

    @Test
    public void cookieAndEntityExtractors() throws Exception {
        // Load the entity-enclosing types on the test side first. The agent-side loader also carries an
        // httpclient (a maven-resolver dependency of the harness); a plugin class that links
        // HttpEntityEnclosingRequest before the test loader has it would bind to that copy and the
        // instanceof check in the extractor would see a different class.
        HttpPost post = new HttpPost("/");
        post.setEntity(new StringEntity("hello"));

        HttpGet get = new HttpGet("/");
        Assertions.assertNull(HttpClient4CookieExtractor.INSTANCE.getCookie(get), "no Cookie header -> null");
        get.addHeader("Cookie", "a=1; b=2");
        Assertions.assertEquals("a=1; b=2", HttpClient4CookieExtractor.INSTANCE.getCookie(get));

        Assertions.assertEquals("hello", HttpClient4EntityExtractor.INSTANCE.getEntity(post));
        Assertions.assertNull(HttpClient4EntityExtractor.INSTANCE.getEntity(get), "GET carries no entity");
    }

}
