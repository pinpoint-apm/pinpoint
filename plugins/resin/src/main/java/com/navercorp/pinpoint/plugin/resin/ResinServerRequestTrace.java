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

package com.navercorp.pinpoint.plugin.resin;

import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestTrace;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class ResinServerRequestTrace implements ServerRequestTrace {
    private final HttpServletRequest request;
    private final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver;

    public ResinServerRequestTrace(final HttpServletRequest request, final RemoteAddressResolver<HttpServletRequest> remoteAddressResolver) {
        this.request = Assert.requireNonNull(request, "request must not be null");
        this.remoteAddressResolver = Assert.requireNonNull(remoteAddressResolver, "remoteAddressResolver must not be null");
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public void setHeader(String name, String value) {
    }

    @Override
    public String getRpcName() {
        return request.getRequestURI();
    }

    @Override
    public String getEndPoint() {
        final int port = request.getServerPort();
        final String endPoint = HostAndPort.toHostAndPortString(request.getServerName(), port);
        return endPoint;
    }

    @Override
    public String getRemoteAddress() {
        final String remoteAddr = remoteAddressResolver.resolve(request);
        return remoteAddr;
    }

    @Override
    public String getAcceptorHost() {
        return NetworkUtils.getHostFromURL(request.getRequestURL().toString());
    }
}