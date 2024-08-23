/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpHeaders;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class HttpRequestAdaptor implements RequestAdaptor<HttpRequestAndContext> {

    @Override
    public String getHeader(HttpRequestAndContext httpRequestAndContext, String name) {
        try {
            final HttpHeaders httpHeaders = httpRequestAndContext.getHttpRequest().headers();
            if (httpHeaders == null) {
                return null;
            }
            final String values = httpHeaders.get(name);
            if (values != null) {
                return values;
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public Collection<String> getHeaderNames(HttpRequestAndContext httpRequestAndContext) {
        try {
            final HttpHeaders httpHeaders = httpRequestAndContext.getHttpRequest().headers();
            if (httpHeaders == null) {
                return Collections.emptyList();
            }
            final Set<String> headerNames = httpHeaders.names();
            if (CollectionUtils.isEmpty(headerNames)) {
                return Collections.emptyList();
            }
            Set<String> values = new HashSet<>(headerNames.size());
            for (String headerName : headerNames) {
                values.add(headerName);
            }
            return values;
        } catch (Exception ignored) {
        }
        return Collections.emptyList();
    }

    @Override
    public String getRpcName(HttpRequestAndContext httpRequestAndContext) {
        try {
            return UriUtils.path(httpRequestAndContext.getHttpRequest().uri());
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getMethodName(HttpRequestAndContext httpRequestAndContext) {
        try {
            return httpRequestAndContext.getHttpRequest().method().name();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getEndPoint(HttpRequestAndContext httpRequestAndContext) {
        try {
            Channel ch = httpRequestAndContext.getContext().channel();
            if (ch != null) {
                return getHost((InetSocketAddress) ch.localAddress());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getRemoteAddress(HttpRequestAndContext httpRequestAndContext) {
        try {
            Channel ch = httpRequestAndContext.getContext().channel();
            if (ch != null) {
                return getHost((InetSocketAddress) ch.remoteAddress());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public String getAcceptorHost(HttpRequestAndContext httpRequestAndContext) {
        try {
            Channel ch = httpRequestAndContext.getContext().channel();
            if (ch != null) {
                return getHost((InetSocketAddress) ch.localAddress());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getHost(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress != null) {
            final InetAddress address = inetSocketAddress.getAddress();
            if (address != null) {
                return HostAndPort.toHostAndPortString(address.getHostAddress(), inetSocketAddress.getPort());
            }
        }
        return null;
    }
}