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

package com.navercorp.pinpoint.it.plugin.utils;

import com.navercorp.pinpoint.testcase.util.SocketUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link WebServer} that keeps the request headers of every call it served, so an HTTP client IT can
 * assert that the agent's propagation headers ({@code Pinpoint-TraceID}, {@code Pinpoint-SpanID}, ...)
 * actually reached the wire — not only that a client span event was recorded. Header names are kept
 * case-insensitively (NanoHTTPD lower-cases them; HTTP header names are case-insensitive anyway).
 * The response echoes the received header names so a test can also verify from the client side.
 */
public class HeaderRecordingWebServer extends WebServer {

    private final List<Map<String, String>> requests = new CopyOnWriteArrayList<>();

    public HeaderRecordingWebServer(String hostname, int port) {
        super(hostname, port);
    }

    public static HeaderRecordingWebServer newTestWebServer() throws IOException {
        final int port = SocketUtils.findAvailableTcpPort(21000);
        final HeaderRecordingWebServer webServer = new HeaderRecordingWebServer(LOCAL_HOST, port);
        webServer.start();
        return webServer;
    }

    @Override
    public Response serve(IHTTPSession session) {
        final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        if (session.getHeaders() != null) {
            headers.putAll(session.getHeaders());
        }
        requests.add(Collections.unmodifiableMap(headers));
        final Response response = newFixedLengthResponse("headers=" + headers.keySet());
        response.addHeader("X-Test-Echo", "echo");
        response.addHeader("X-Test-Multi", "a");
        return response;
    }

    /** Headers of every request served so far, oldest first. */
    public List<Map<String, String>> getRequestHeaders() {
        return new ArrayList<>(requests);
    }

    /** Headers of the most recent request, or an empty map when nothing was served. */
    public Map<String, String> getLastRequestHeaders() {
        if (requests.isEmpty()) {
            return Collections.emptyMap();
        }
        return requests.get(requests.size() - 1);
    }

    public void clear() {
        requests.clear();
    }
}
