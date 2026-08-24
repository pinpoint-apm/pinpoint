/*
 * Copyright 2026 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.ktor.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KtorClientRequestAdaptorTest {

    private final KtorClientRequestAdaptor adaptor = new KtorClientRequestAdaptor();

    @Test
    void buildDestinationIdFromHostAndPort() {
        FakeUrl url = new FakeUrl("api.example.com", 9443, "http://api.example.com:9443/v1/images");
        FakeHeadersRequest request = new FakeHeadersRequest(url);

        assertEquals("api.example.com:9443", adaptor.getDestinationId(request));
    }

    @Test
    void getUrlReturnsBuildString() {
        FakeUrl url = new FakeUrl("api.example.com", 8080, "http://api.example.com:8080/v1/images");
        FakeHeadersRequest request = new FakeHeadersRequest(url);

        assertEquals("http://api.example.com:8080/v1/images", adaptor.getUrl(request));
    }

    @Test
    void unknownInputYieldsSafeDefaults() {
        assertEquals("Unknown", adaptor.getDestinationId(null));
        assertEquals("Unknown", adaptor.getDestinationId(new Object()));
        assertNull(adaptor.getUrl(null));
        assertNull(adaptor.getUrl(new Object()));
    }

    @Test
    void emptyHostFallsBackToUnknown() {
        FakeUrl url = new FakeUrl("", 443, "https://:443/");
        FakeHeadersRequest request = new FakeHeadersRequest(url);

        assertEquals("Unknown", adaptor.getDestinationId(request));
    }

    @Test
    void ipv6HostIsBracketed() {
        FakeUrl url = new FakeUrl("::1", 8080, "http://[::1]:8080/");
        FakeHeadersRequest request = new FakeHeadersRequest(url);

        assertEquals("[::1]:8080", adaptor.getDestinationId(request));
    }

    /** Stand-in for Ktor's Url class so we can exercise accessor reflection without Ktor on the classpath. */
    public static class FakeUrl {
        private final String host;
        private final int port;
        private final String builtUrl;

        FakeUrl(String host, int port, String builtUrl) {
            this.host = host;
            this.port = port;
            this.builtUrl = builtUrl;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String buildString() {
            return builtUrl;
        }

        public FakeUrlProtocol getProtocolOrNull() {
            return null;
        }
    }

    public static class FakeUrlProtocol {
        public int getDefaultPort() {
            return 443;
        }
    }

    public static class FakeHeadersRequest {
        private final FakeUrl url;

        FakeHeadersRequest(FakeUrl url) {
            this.url = url;
        }

        public FakeUrl getUrl() {
            return url;
        }
    }
}
