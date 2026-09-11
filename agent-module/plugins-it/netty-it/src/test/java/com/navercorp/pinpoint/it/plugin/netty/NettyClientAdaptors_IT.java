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

package com.navercorp.pinpoint.it.plugin.netty;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.it.plugin.utils.AgentPath;
import com.navercorp.pinpoint.plugin.netty.NettyClientRequestWrapper;
import com.navercorp.pinpoint.plugin.netty.interceptor.http.HttpMessageClientHeaderAdaptor;
import com.navercorp.pinpoint.test.plugin.Dependency;
import com.navercorp.pinpoint.test.plugin.ImportPlugin;
import com.navercorp.pinpoint.test.plugin.PinpointAgent;
import com.navercorp.pinpoint.test.plugin.PluginTest;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Calls the netty plugin's client request wrapper and header adaptor directly against real Netty HTTP codec
 * objects of every release in range, including 4.2 - no interceptor, no channel. The remote address side of
 * the wrapper needs a connected channel and stays with the interceptor IT; here only the fallback is pinned.
 */
@PluginTest
@PinpointAgent(AgentPath.PATH)
// netty-codec-http pulls the core modules; netty-all is an empty aggregator from 4.1.7x on and has no 4.2 jar.
@Dependency({"io.netty:netty-codec-http:[4.1.0.Final,4.2.max]"})
@ImportPlugin({"com.navercorp.pinpoint:pinpoint-netty-plugin"})
public class NettyClientAdaptors_IT {

    private final ClientHeaderAdaptor<HttpMessage> headerAdaptor = new HttpMessageClientHeaderAdaptor();

    @Test
    public void requestWrapper_urlFromTheRequest_unknownDestinationWithoutAChannel() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/path?q=1");

        ClientRequestWrapper wrapper = new NettyClientRequestWrapper(request, null);
        Assertions.assertEquals("/path?q=1", wrapper.getUrl());
        Assertions.assertEquals("Unknown", wrapper.getDestinationId(), "no channel context -> Unknown");
    }

    @Test
    public void headerAdaptor_setGetContains() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/");

        Assertions.assertFalse(headerAdaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertEquals("", headerAdaptor.getHeader(request, "Pinpoint-TraceID"), "absent header reads as empty string");

        headerAdaptor.setHeader(request, "Pinpoint-TraceID", "agent^1^1");
        Assertions.assertTrue(headerAdaptor.contains(request, "Pinpoint-TraceID"));
        Assertions.assertTrue(headerAdaptor.contains(request, "pinpoint-traceid"), "header names are case-insensitive");
        Assertions.assertEquals("agent^1^1", headerAdaptor.getHeader(request, "Pinpoint-TraceID"));

        // The netty adaptor only writes a header that is not there yet (headers().contains guard).
        headerAdaptor.setHeader(request, "Pinpoint-TraceID", "agent^1^2");
        Assertions.assertEquals("agent^1^1", headerAdaptor.getHeader(request, "Pinpoint-TraceID"), "an existing header is kept, not replaced");
        Assertions.assertEquals(1, request.headers().getAll("Pinpoint-TraceID").size());
    }
}
