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

package com.navercorp.pinpoint.bootstrap.plugin.request;

import com.navercorp.pinpoint.bootstrap.plugin.RequestWrapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jaehong.kim
 */
public class ServletServerRequestWrapperFactoryTest {
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    public static final String UNKNOWN = "unknown";

    @Test
    public void getRemoteAddress0() throws Exception {
        final RemoteAddressResolver remoteAddressResolver = RemoteAddressResolverFactory.newRemoteAddressResolver("x-forwarded-for", "unknown");
        final ServletServerRequestWrapperFactory factory = new ServletServerRequestWrapperFactory(remoteAddressResolver);
        final RequestWrapper requestWrapper = mock(RequestWrapper.class);
        when(requestWrapper.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1");

        final String remoteAddr = "127.0.0.2";
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        final ServletServerRequestWrapper servletServerRequestWrapper = factory.get(requestWrapper, "/test", "localhost", 80, remoteAddr, new StringBuffer("http://localhost"), "GET", parameterMap);
        assertEquals("127.0.0.1", servletServerRequestWrapper.getRemoteAddress());
    }

    @Test
    public void getRemoteAddress1() throws Exception {
        final RemoteAddressResolver remoteAddressResolver = RemoteAddressResolverFactory.newRemoteAddressResolver("x-forwarded-for", "unknown");
        final ServletServerRequestWrapperFactory factory = new ServletServerRequestWrapperFactory(remoteAddressResolver);
        final RequestWrapper requestWrapper = mock(RequestWrapper.class);
        when(requestWrapper.getHeader(X_FORWARDED_FOR)).thenReturn("127.0.0.1, proxy1, proxy2");

        final String remoteAddr = "127.0.0.2";
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        final ServletServerRequestWrapper servletServerRequestWrapper = factory.get(requestWrapper, "/test", "localhost", 80, remoteAddr, new StringBuffer("http://localhost"), "GET", parameterMap);
        assertEquals("127.0.0.1", servletServerRequestWrapper.getRemoteAddress());
    }

    @Test
    public void getRemoteAddress2() throws Exception {
        final RemoteAddressResolver remoteAddressResolver = RemoteAddressResolverFactory.newRemoteAddressResolver("x-forwarded-for", "unknown");
        final ServletServerRequestWrapperFactory factory = new ServletServerRequestWrapperFactory(remoteAddressResolver);
        final RequestWrapper requestWrapper = mock(RequestWrapper.class);

        final String remoteAddr = "127.0.0.2";
        final Map<String, String[]> parameterMap = new HashMap<String, String[]>();
        final ServletServerRequestWrapper servletServerRequestWrapper = factory.get(requestWrapper, "/test", "localhost", 80, remoteAddr, new StringBuffer("http://localhost"), "GET", parameterMap);
        assertEquals("127.0.0.2", servletServerRequestWrapper.getRemoteAddress());
    }
}