/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UriUtilsTest {

    @Test
    public void path() throws Exception {
        assertEquals("", UriUtils.path(""));
        assertEquals("/", UriUtils.path("/"));
        assertEquals("//", UriUtils.path("//"));
        assertEquals("///", UriUtils.path("///"));
        assertEquals("/foo", UriUtils.path("/foo"));
        assertEquals("/foo/", UriUtils.path("/foo/"));
        assertEquals("/", UriUtils.path("/?bar"));
        assertEquals("/foo", UriUtils.path("/foo?bar"));
        assertEquals("/", UriUtils.path("/#bar"));
        assertEquals("/foo", UriUtils.path("/foo#bar"));
        assertEquals("/foo", UriUtils.path("/foo?bar#baz"));
        assertEquals("/foo", UriUtils.path("/foo?bar={}"));
    }

    @Test
    public void params() throws Exception {
        assertEquals(null, UriUtils.params("/"));
        assertEquals(null, UriUtils.params("/foo"));
        assertEquals(null, UriUtils.params("/foo&bar"));
        assertEquals("bar=baz", UriUtils.params("/foo?bar=baz"));
        assertEquals("bar=baz", UriUtils.params("/?bar=baz"));
        assertEquals("bar=baz", UriUtils.params("?bar=baz"));
        assertEquals("bar", UriUtils.params("?bar"));
        assertEquals("bar={}", UriUtils.params("/foo?bar={}"));
    }
}
