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

package com.navercorp.pinpoint.plugin.vertx;

import com.navercorp.pinpoint.bootstrap.context.RemoteAddressResolver;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.NetworkUtils;
import com.navercorp.pinpoint.common.util.Assert;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author jaehong.kim
 */
public class VertxHttpServerServerRequestWrapper implements ServerRequestWrapper {
    private final HttpServerRequest request;
    private final RemoteAddressResolver<HttpServerRequest> remoteAddressResolver;

    public VertxHttpServerServerRequestWrapper(final HttpServerRequest request, final RemoteAddressResolver<HttpServerRequest> remoteAddressResolver) {
        this.request = Assert.requireNonNull(request, "request must not be null");
        this.remoteAddressResolver = Assert.requireNonNull(remoteAddressResolver, "remoteAddressResolver must not be null");
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public String getRpcName() {
        return this.request.path();
    }

    @Override
    public String getEndPoint() {
        if (request.localAddress() != null) {
            final int port = request.localAddress().port();
            if (port <= 0) {
                return request.host();
            } else {
                return request.host() + ":" + port;
            }
        }
        return null;
    }

    @Override
    public String getRemoteAddress() {
        final String remoteAddr = this.remoteAddressResolver.resolve(request);
        return remoteAddr;
    }

    @Override
    public String getAcceptorHost() {
        return NetworkUtils.getHostFromURL(request.uri().toString());
    }
}