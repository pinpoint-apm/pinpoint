/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jetty;

import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import org.eclipse.jetty.server.Request;

/**
 * @author jaehong.kim
 */
public abstract class JettyServerRequestTrace implements ServerRequestTrace {
    private final Request request;

    public JettyServerRequestTrace(Request request) {
        this.request = Assert.requireNonNull(request, "request must not be null");
    }

    public abstract String _getHeader(String name);

    @Override
    public String getHeader(String name) {
        return _getHeader(name);
    }

    @Override
    public void setHeader(final String name, final String value) {
    }

    @Override
    public String getRpcName() {
        final String requestURL = request.getRequestURI();
        return requestURL;
    }

    @Override
    public String getEndPoint() {
        final int port = request.getServerPort();
        final String endPoint = HostAndPort.toHostAndPortString(request.getServerName(), port);
        return endPoint;
    }

    @Override
    public String getRemoteAddress() {
        final String remoteAddr = request.getRemoteAddr();
        return remoteAddr;
    }

    @Override
    public String getAcceptorHost() {
        return NetworkUtils.getHostFromURL(request.getRequestURL().toString());
    }
}