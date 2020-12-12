/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import reactor.netty.http.HttpOperations;
import reactor.netty.http.client.HttpClientRequest;

import java.net.InetSocketAddress;

/**
 * @author jaehong.kim
 */
public class HttpClientRequestWrapper implements ClientRequestWrapper {

    private final HttpClientRequest request;

    public HttpClientRequestWrapper(final HttpClientRequest request) {
        this.request = request;
    }

    @Override
    public String getDestinationId() {
        if (request instanceof HttpOperations) {
            try {
                final HttpOperations httpOperations = (HttpOperations) request;
                final InetSocketAddress inetSocketAddress = httpOperations.address();
                if (inetSocketAddress != null) {
                    final String hostName = SocketAddressUtils.getHostNameFirst(inetSocketAddress);
                    if (hostName != null) {
                        return HostAndPort.toHostAndPortString(hostName, inetSocketAddress.getPort());
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "UNKNOWN";
    }

    @Override
    public String getUrl() {
        if (request instanceof HttpOperations) {
            try {
                final StringBuilder sb = new StringBuilder();
                final HttpOperations httpOperations = (HttpOperations) request;
                final InetSocketAddress inetSocketAddress = httpOperations.address();
                if (inetSocketAddress != null) {
                    final String hostName = SocketAddressUtils.getHostNameFirst(inetSocketAddress);
                    if (hostName != null) {
                        sb.append(hostName).append(":").append(inetSocketAddress.getPort());
                    }
                }
                final String uri = httpOperations.uri();
                if (uri != null) {
                    sb.append(uri);
                }
                return sb.toString();
            } catch (Exception ignored) {
            }
        }

        return null;
    }
}
