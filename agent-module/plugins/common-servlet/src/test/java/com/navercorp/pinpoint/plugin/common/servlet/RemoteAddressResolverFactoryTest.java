/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.common.servlet;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.plugin.common.servlet.util.HttpServletRequestAdaptor;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RemoteAddressResolverFactoryTest {
    public static final String FORWARDED = "forwarded";
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String UNKNOWN = "unknown";

    @Test
    public void getRemoteAddress0() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, X_FORWARDED_FOR, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("127.0.0.1", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress1() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, X_FORWARDED_FOR, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1, proxy1, proxy2");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("127.0.0.1", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress2() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, X_FORWARDED_FOR, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("127.0.0.2", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress3() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=\"_gazonk\"");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("_gazonk", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress4() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("For=\"[2001:db8:cafe::17]:4711\"");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("[2001:db8:cafe::17]", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress5() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.60;proto=http;by=203.0.113.43");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("192.0.2.60", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress6() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43, for=198.51.100.17");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("192.0.2.43", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress7() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43,for=198.51.100.17");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("192.0.2.43", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress8() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, FORWARDED, UNKNOWN);
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        // invalid format
        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("fjlafjlkajflkfk");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("fjlafjlkajflkfk", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress9() throws Exception {
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        RequestAdaptor<HttpServletRequest> requestAdaptor1 = new HttpServletRequestAdaptor();
        requestAdaptor1 = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor1, "forwarded, x-forwarded-for", UNKNOWN);
        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43");
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("198.51.100.17");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("192.0.2.43", requestAdaptor1.getRemoteAddress(httpServletRequest));

        RequestAdaptor<HttpServletRequest> requestAdaptor2 = new HttpServletRequestAdaptor();
        requestAdaptor2 = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor2, "x-forwarded-for, forwarded", UNKNOWN);
        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43");
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("198.51.100.17");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("198.51.100.17", requestAdaptor2.getRemoteAddress(httpServletRequest));

        RequestAdaptor<HttpServletRequest> requestAdaptor3 = new HttpServletRequestAdaptor();
        requestAdaptor3 = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor3, "", UNKNOWN);
        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43");
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("198.51.100.17");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("127.0.0.2", requestAdaptor3.getRemoteAddress(httpServletRequest));

        RequestAdaptor<HttpServletRequest> requestAdaptor4 = new HttpServletRequestAdaptor();
        requestAdaptor4 = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor3, "X-RealIP", UNKNOWN);
        when(httpServletRequest.getHeader(FORWARDED)).thenReturn("for=192.0.2.43");
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("198.51.100.17");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("127.0.0.2", requestAdaptor4.getRemoteAddress(httpServletRequest));
    }
}
