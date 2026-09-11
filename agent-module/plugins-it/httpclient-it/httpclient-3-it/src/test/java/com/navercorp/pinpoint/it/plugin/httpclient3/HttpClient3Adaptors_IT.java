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

package com.navercorp.pinpoint.it.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3CookieExtractor;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3EntityExtractor;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3RequestWrapper;
import com.navercorp.pinpoint.plugin.httpclient3.HttpMethodClientHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Calls the httpclient3 plugin's request wrapper, header adaptor and extractors directly against real
 * commons-httpclient 3.x objects of every release in range - no interceptor, no HTTP call. The
 * {@code HttpConnection} is constructed but never opened: the wrapper only reads host, port and protocol.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
@Dependency({"commons-httpclient:commons-httpclient:[3.0,3.1]"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-httpclient3-plugin"})
public class HttpClient3Adaptors_IT {

    private final ClientHeaderAdaptor<HttpMethod> headerAdaptor = new HttpMethodClientHeaderAdaptor();

    @Test
    public void requestWrapper_absoluteUri_ignoresTheConnection() throws Exception {
        GetMethod get = new GetMethod("http://host.example:8080/path?q=1");
        HttpConnection otherConnection = new HttpConnection("other.example", 9090, Protocol.getProtocol("http"));

        ClientRequestWrapper wrapper = new HttpClient3RequestWrapper(get, otherConnection);
        Assertions.assertEquals("host.example:8080", wrapper.getDestinationId());
        Assertions.assertEquals("http://host.example:8080/path?q=1", wrapper.getUrl());

        Assertions.assertEquals("host.example:8080", new HttpClient3RequestWrapper(get, null).getDestinationId(), "no connection: still from the absolute uri");
    }

    @Test
    public void requestWrapper_relativeUri_usesTheConnection_andDropsTheDefaultPort() throws Exception {
        GetMethod get = new GetMethod("/path?q=1");

        HttpConnection connection = new HttpConnection("host.example", 8080, Protocol.getProtocol("http"));
        ClientRequestWrapper wrapper = new HttpClient3RequestWrapper(get, connection);
        Assertions.assertEquals("host.example:8080", wrapper.getDestinationId());
        Assertions.assertEquals("http://host.example:8080/path?q=1", wrapper.getUrl());

        HttpConnection defaultPort = new HttpConnection("host.example", 80, Protocol.getProtocol("http"));
        ClientRequestWrapper wrapperDefault = new HttpClient3RequestWrapper(get, defaultPort);
        Assertions.assertEquals("host.example", wrapperDefault.getDestinationId(), "protocol default port is omitted");
        Assertions.assertEquals("http://host.example/path?q=1", wrapperDefault.getUrl());
    }

    @Test
    public void headerAdaptor_setGetContains() {
        HttpMethod get = new GetMethod("/");

        Assertions.assertFalse(headerAdaptor.contains(get, "Pinpoint-TraceID"));
        Assertions.assertEquals("", headerAdaptor.getHeader(get, "Pinpoint-TraceID"), "absent header reads as empty string");

        headerAdaptor.setHeader(get, "Pinpoint-TraceID", "agent^1^1");
        headerAdaptor.setHeader(get, "Pinpoint-TraceID", "agent^1^2");

        Assertions.assertTrue(headerAdaptor.contains(get, "Pinpoint-TraceID"));
        Assertions.assertTrue(headerAdaptor.contains(get, "pinpoint-traceid"), "header names are case-insensitive");
        Assertions.assertEquals("agent^1^2", headerAdaptor.getHeader(get, "Pinpoint-TraceID"), "set replaces");
        Assertions.assertEquals(1, get.getRequestHeaders("Pinpoint-TraceID").length, "set must not accumulate values");
    }

    @Test
    public void cookieAndEntityExtractors() throws Exception {
        GetMethod get = new GetMethod("/");
        Assertions.assertNull(HttpClient3CookieExtractor.INSTANCE.getCookie(get), "no Cookie header -> null");
        get.setRequestHeader("Cookie", "a=1; b=2");
        Assertions.assertEquals("a=1; b=2", HttpClient3CookieExtractor.INSTANCE.getCookie(get));

        Assertions.assertNull(HttpClient3EntityExtractor.INSTANCE.getEntity(get), "GET carries no entity");
        PostMethod post = new PostMethod("/");
        post.setRequestEntity(new StringRequestEntity("hello", "text/plain", "UTF-8"));
        String entity = HttpClient3EntityExtractor.INSTANCE.getEntity(post);
        Assertions.assertNotNull(entity);
        Assertions.assertTrue(entity.contains("hello"), "entity: " + entity);
    }
}
