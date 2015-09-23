/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import org.junit.Assert;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author emeroad
 */
public class RealIpHeaderResolverTest {

    private final String xForwardedFor = StandardHostValveInvokeInterceptor.RealIpHeaderResolver.X_FORWARDED_FOR;

    @Test
    public void testProxyHeader() {

        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getHeader(xForwardedFor)).thenReturn("127.0.0.1");
        when(mock.getRemoteAddr()).thenReturn("127.0.0.2");


        StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest> realIpHeaderResolver = new StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest>();
        String resolve = realIpHeaderResolver.resolve(mock);
        Assert.assertEquals(resolve, "127.0.0.1");


    }

    @Test
    public void testProxyHeader_proxy1() {

        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getHeader(xForwardedFor)).thenReturn("127.0.0.1, proxy1, proxy2");
        when(mock.getRemoteAddr()).thenReturn("127.0.0.2");

        StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest> realIpHeaderResolver = new StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest>();
        String resolve = realIpHeaderResolver.resolve(mock);
        Assert.assertEquals(resolve, "127.0.0.1");

    }

    @Test
    public void testProxyHeader_proxy2() {

        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getHeader(xForwardedFor)).thenReturn("127.0.0.1,");
        when(mock.getRemoteAddr()).thenReturn("127.0.0.2");

        StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest> realIpHeaderResolver = new StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest>();
        String resolve = realIpHeaderResolver.resolve(mock);
        Assert.assertEquals(resolve, "127.0.0.1");
    }

    @Test
    public void testProxyHeader_header_not_exist() {

        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getRemoteAddr()).thenReturn("127.0.0.2");


        StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest> realIpHeaderResolver = new StandardHostValveInvokeInterceptor.RealIpHeaderResolver<HttpServletRequest>();
        String resolve = realIpHeaderResolver.resolve(mock);
        Assert.assertEquals(resolve, "127.0.0.2");

    }

}
