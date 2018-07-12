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

package com.navercorp.pinpoint.plugin.common.servlet.util;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpServletRequestAdaptor implements RequestAdaptor<HttpServletRequest> {

    public HttpServletRequestAdaptor() {
    }

    @Override
    public String getHeader(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRpcName(HttpServletRequest request) {
        return request.getRequestURI();
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
}
