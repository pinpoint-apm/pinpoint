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

package com.navercorp.pinpoint.pluginit.utils;

import com.google.common.collect.Maps;
import com.navercorp.pinpoint.common.util.IOUtils;
import fi.iki.elonen.NanoHTTPD;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

/**
 * @author WonChul Heo(heowc)
 */
public class WebServerTest {

    private static WebServer webServer;

    @BeforeClass
    public static void newTestWebServer() throws Exception {
        webServer = WebServer.newTestWebServer();
    }

    @AfterClass
    public static void cleanup() {
        WebServer.cleanup(webServer);
    }

    @Test
    public void testGetHostAndPort() {
        assertThat(webServer.getHostAndPort(), startsWith("localhost:"));
    }

    @Test
    public void testGetCallHttpUrl() {
        assertThat(webServer.getCallHttpUrl(), startsWith("http://localhost:"));
    }

    @Test
    public void testHostname() {
        assertThat(webServer.getHostname(), is("localhost"));
    }

    @Test
    public void testServe() {
        HashMap<String, List<String>> params = new HashMap<String, List<String>>();
        NanoHTTPD.Response response = serve(params);
        assertResponse(response, "{}");

        params = new HashMap<String, List<String>>();
        params.put("foo", Collections.singletonList("bar"));
        response = serve(params);
        assertResponse(response, "{foo=[bar]}");

        params = new HashMap<String, List<String>>();
        params.put("a", Collections.singletonList("b"));
        params.put("c", Collections.singletonList("d"));
        response = serve(params);
        assertResponse(response, "{a=[b], c=[d]}");
    }

    private NanoHTTPD.Response serve(final Map<String, List<String>> params) {
        return webServer.serve(new NanoHTTPD.IHTTPSession() {
            @Override
            public void execute() {
                // ignored
            }

            @Override
            public NanoHTTPD.CookieHandler getCookies() {
                return null;
            }

            @Override
            public Map<String, String> getHeaders() {
                return Maps.newHashMap();
            }

            @Override
            public InputStream getInputStream() {
                return null;
            }

            @Override
            public NanoHTTPD.Method getMethod() {
                return NanoHTTPD.Method.GET;
            }

            @Override
            public Map<String, String> getParms() {
                return null;
            }

            @Override
            public Map<String, List<String>> getParameters() {
                return params;
            }

            @Override
            public String getQueryParameterString() {
                return null;
            }

            @Override
            public String getUri() {
                return null;
            }

            @Override
            public void parseBody(Map<String, String> files) {
                // ignored
            }

            @Override
            public String getRemoteIpAddress() {
                return null;
            }

            @Override
            public String getRemoteHostName() {
                return null;
            }
        });
    }

    private static void assertResponse(NanoHTTPD.Response response, String responseData) {
        try {
            assertThat(response.getStatus().getRequestStatus(), is(NanoHTTPD.Response.Status.OK.getRequestStatus()));
            assertThat(response.getMimeType(), is(NanoHTTPD.MIME_HTML));
            final String actualData = new String(IOUtils.toByteArray(response.getData()), Charset.defaultCharset());
            assertThat(actualData, is(responseData));
        } catch (IOException e) {
            // ignored
        }
    }
}
