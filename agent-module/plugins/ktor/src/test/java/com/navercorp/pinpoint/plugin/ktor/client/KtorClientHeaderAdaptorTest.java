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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KtorClientHeaderAdaptorTest {

    private final KtorClientHeaderAdaptor adaptor = new KtorClientHeaderAdaptor();

    @Test
    void setGetContainsRoundTrip() {
        MapBackedHeaders headers = new MapBackedHeaders();
        HeadersCarrier request = new HeadersCarrier(headers);

        assertFalse(adaptor.contains(request, "x-pinpoint-traceid"));

        adaptor.setHeader(request, "x-pinpoint-traceid", "agent^123^1");
        adaptor.setHeader(request, "x-pinpoint-spanid", "42");

        assertTrue(adaptor.contains(request, "x-pinpoint-traceid"));
        assertEquals("agent^123^1", adaptor.getHeader(request, "x-pinpoint-traceid"));
        assertEquals("42", headers.map.get("x-pinpoint-spanid"));
    }

    @Test
    void nullSafety() {
        assertEquals("", adaptor.getHeader(null, "anything"));
        assertFalse(adaptor.contains(null, "anything"));
        // must not throw
        adaptor.setHeader(null, "key", "value");
    }

    @Test
    void missingHeaderReturnsEmpty() {
        HeadersCarrier request = new HeadersCarrier(new MapBackedHeaders());
        assertEquals("", adaptor.getHeader(request, "absent"));
        assertFalse(adaptor.contains(request, "absent"));
    }

    /** Ktor's {@code getHeaders()} returns a Headers instance — we substitute a minimal carrier. */
    public static class HeadersCarrier {
        private final MapBackedHeaders headers;

        HeadersCarrier(MapBackedHeaders headers) {
            this.headers = headers;
        }

        public MapBackedHeaders getHeaders() {
            return headers;
        }
    }

    public static class MapBackedHeaders {
        final Map<String, String> map = new HashMap<>();

        public String get(String name) {
            return map.get(name);
        }

        public Boolean contains(String name) {
            return map.containsKey(name);
        }

        public void set(String name, String value) {
            map.put(name, value);
        }
    }
}
