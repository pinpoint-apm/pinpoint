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

package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.http.HttpRequest;
import org.apache.http.RequestLine;

/**
 * @author jaehong.kim
 */
public class HttpClient4RequestWrapper implements ClientRequestWrapper {

    private final HttpRequest httpRequest;
    private final String hostName;
    private final int port;

    public HttpClient4RequestWrapper(final HttpRequest httpRequest, final String hostName, final int port) {
        this.httpRequest = Assert.requireNonNull(httpRequest, "httpRequest");
        this.hostName = hostName;
        this.port = port;
    }


    @Override
    public String getDestinationId() {
        return getEndpoint(this.hostName, this.port);
    }

    private static String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "Unknown";
        }
        if (port < 0) {
            return host;
        }
        final StringBuilder sb = new StringBuilder(host.length() + 8);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }


    @Override
    public String getUrl() {
        final RequestLine requestLine = this.httpRequest.getRequestLine();
        if (requestLine != null) {
            return requestLine.getUri();
        }
        return null;
    }

}