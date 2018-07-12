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

import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.RemoteAddressResolverFactory;
import com.navercorp.pinpoint.plugin.common.servlet.util.HttpServletRequestAdaptor;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RemoteAddressResolverFactoryTest {
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String UNKNOWN = "unknown";

    @Test
    public void getRemoteAddress0() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, "x-forwarded-for", "unknown");
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");
        assertEquals("127.0.0.1", requestAdaptor.getRemoteAddress(httpServletRequest));
    }


    @Test
    public void getRemoteAddress1() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, "x-forwarded-for", "unknown");
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(httpServletRequest.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1, proxy1, proxy2");

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("127.0.0.1", requestAdaptor.getRemoteAddress(httpServletRequest));
    }

    @Test
    public void getRemoteAddress2() throws Exception {
        RequestAdaptor<HttpServletRequest> requestAdaptor = new HttpServletRequestAdaptor();
        requestAdaptor = RemoteAddressResolverFactory.wrapRealIpSupport(requestAdaptor, "x-forwarded-for", "unknown");
        final HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);

        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.2");

        assertEquals("127.0.0.2", requestAdaptor.getRemoteAddress(httpServletRequest));
    }
}