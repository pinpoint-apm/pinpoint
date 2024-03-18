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

package com.navercorp.pinpoint.plugin.common.servlet.jakarta.util;

import com.navercorp.pinpoint.bootstrap.plugin.request.CookieAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.CookieFilter;
import com.navercorp.pinpoint.bootstrap.plugin.request.CookieSupportAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpServletRequestAdaptor implements RequestAdaptor<HttpServletRequest>, CookieSupportAdaptor<HttpServletRequest> {

    private final CookieFilter<Cookie> cookieFilter = new CookieFilter<Cookie>() {
        @Override
        protected CookieAdaptor newCookieAdaptor(Cookie cookie) {
            return new HttpServletCookieAdaptor(cookie);
        }

        @Override
        protected String getName(Cookie cookie) {
            return cookie.getName();
        }
    };

    public HttpServletRequestAdaptor() {
    }

    @Override
    public String getHeader(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    @Override
    public Collection<String> getHeaderNames(HttpServletRequest request) {
        final Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return Collections.emptySet();
        }
        return Collections.list(headerNames);
    }

    @Override
    public String getRpcName(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @Override
    public String getMethodName(HttpServletRequest request) {
        return request.getMethod();
    }

    @Override
    public String getEndPoint(HttpServletRequest request) {
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        return HostAndPort.toHostAndPortString(serverName, serverPort);
    }

    @Override
    public String getRemoteAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    @Override
    public String getAcceptorHost(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        final String acceptorHost = url != null ? NetworkUtils.getHostFromURL(url.toString()) : null;
        return acceptorHost;
    }

    @Override
    public List<CookieAdaptor> getCookie(HttpServletRequest request) {
        final Cookie[] cookies = request.getCookies();
        return cookieFilter.wrap(cookies);
    }

    @Override
    public List<CookieAdaptor> getCookie(HttpServletRequest request, String[] cookieNames) {
        final Cookie[] cookies = request.getCookies();
        return cookieFilter.filter(cookies, cookieNames);
    }

}
