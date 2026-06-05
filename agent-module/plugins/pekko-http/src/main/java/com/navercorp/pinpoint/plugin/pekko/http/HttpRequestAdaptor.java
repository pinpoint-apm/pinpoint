/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.pekko.http;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.pekko.http.javadsl.model.Host;
import org.apache.pekko.http.javadsl.model.HttpHeader;
import org.apache.pekko.http.javadsl.model.HttpRequest;
import org.apache.pekko.http.javadsl.model.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HttpRequestAdaptor implements RequestAdaptor<HttpRequest> {

    private static final String UNKNOWN = "Unknown";

    // https://pekko.apache.org/docs/pekko-http/current/routing-dsl/directives/misc-directives/extractClientIP.html
    // X-Forwarded-For, Remote-Address, or X-Real-IP

    private static final String DEFAULT_REMOTE_ADDRESS_HEADER = "Remote-Address";

    private final String remoteAddressHeader;

    public HttpRequestAdaptor(PekkoHttpConfig config) {
        this.remoteAddressHeader = getRealIpHeader(config);
    }

    private String getRealIpHeader(PekkoHttpConfig config) {
        String realIpHeader = config.getRealIpHeader();
        return StringUtils.defaultIfEmpty(realIpHeader, DEFAULT_REMOTE_ADDRESS_HEADER);
    }

    @Override
    public String getHeader(HttpRequest request, String name) {
        return getHeader(request, name, null);
    }

    @Override
    public Collection<String> getHeaderNames(HttpRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }
        final Iterable<HttpHeader> headers = request.getHeaders();
        if (headers == null) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (HttpHeader header : headers) {
            names.add(header.name());
        }
        return names;
    }

    private String getHeader(HttpRequest request, String name, String defaultValue) {
        if (request == null) {
            return defaultValue;
        }

        Optional<HttpHeader> optional = request.getHeader(name);
        if (optional == null) {
            return defaultValue;
        }

        HttpHeader header = optional.orElse(null);
        if (header == null) {
            return defaultValue;
        }

        String value = header.value();
        if (value == null) {
            return defaultValue;
        }

        return value;
    }

    @Override
    public String getRpcName(HttpRequest request) {
        Uri uri = request.getUri();
        if (validateRpcName(uri)) {
            return uri.getPathString();
        }
        return UNKNOWN;
    }

    @Override
    public String getMethodName(HttpRequest request) {
        return request.method().name();
    }

    private boolean validateRpcName(Uri uri) {
        if (uri == null) {
            return false;
        }
        return StringUtils.hasText(uri.getPathString());
    }

    @Override
    public String getEndPoint(HttpRequest request) {
        Uri uri = request.getUri();
        if (validateEndPoint(uri)) {
            Host host = uri.getHost();
            return HostAndPort.toHostAndPortString(host.address(), uri.getPort());
        }
        return UNKNOWN;
    }

    private boolean validateEndPoint(Uri uri) {
        if (uri == null) {
            return false;
        }

        Host host = uri.getHost();
        if (host == null) {
            return false;
        }

        String hostAddress = host.address();
        return StringUtils.hasText(hostAddress);
    }

    @Override
    public String getRemoteAddress(HttpRequest request) {
        return getHeader(request, remoteAddressHeader, "");
    }

    @Override
    public String getAcceptorHost(HttpRequest request) {
        return getHeader(request, Header.HTTP_HOST.toString());
    }
}
