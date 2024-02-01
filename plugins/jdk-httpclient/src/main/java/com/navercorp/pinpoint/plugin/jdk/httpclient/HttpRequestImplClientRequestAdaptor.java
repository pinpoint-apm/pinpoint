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

package com.navercorp.pinpoint.plugin.jdk.httpclient;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import jdk.internal.net.http.HttpRequestImpl;

import java.net.URI;

public class HttpRequestImplClientRequestAdaptor implements ClientRequestAdaptor<HttpRequestImpl> {

    public static String getHost(HttpRequestImpl httpRequest) {
        final URI uri = httpRequest.uri();
        if (uri != null) {
            final String host = uri.getHost();
            final int port = uri.getPort();
            if (host != null) {
                return HttpRequestImplClientRequestAdaptor.getEndpoint(host, port);
            }
        }
        return null;
    }

    public static String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "UNKNOWN";
        }
        if (port < 0) {
            return host;
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
    public String getDestinationId(HttpRequestImpl httpRequest) {
        final URI uri = httpRequest.uri();
        if (uri != null) {
            final String host = uri.getHost();
            final int port = uri.getPort();
            return getEndpoint(host, port);
        }
        return "UNKNOWN";
    }

    @Override
    public String getUrl(HttpRequestImpl httpRequest) {
        final URI uri = httpRequest.uri();
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }
}