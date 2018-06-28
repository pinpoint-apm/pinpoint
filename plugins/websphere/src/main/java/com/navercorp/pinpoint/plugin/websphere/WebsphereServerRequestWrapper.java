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

package com.navercorp.pinpoint.plugin.websphere;

import com.ibm.websphere.servlet.request.IRequest;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class WebsphereServerRequestWrapper implements ServerRequestWrapper {
    private final IRequest request;

    public WebsphereServerRequestWrapper(final IRequest request) {
        this.request = Assert.requireNonNull(request, "");
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public String getRpcName() {
        return this.request.getRequestURI();
    }

    @Override
    public String getEndPoint() {
        final int port = request.getServerPort();
        final String endPoint = HostAndPort.toHostAndPortString(request.getServerName(), port);
        return endPoint;
    }

    @Override
    public String getRemoteAddress() {
        return request.getRemoteAddr();
    }

    @Override
    public String getAcceptorHost() {
        return NetworkUtils.getHostFromURL(request.getRequestURI());
    }
}