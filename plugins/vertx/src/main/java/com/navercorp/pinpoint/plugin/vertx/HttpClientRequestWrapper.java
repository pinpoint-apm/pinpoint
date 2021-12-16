/*
 * Copyright 2021 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import io.vertx.core.http.HttpClientRequest;

import java.util.Objects;

public class HttpClientRequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpClientRequest httpRequest;
    private final String host;

    public HttpClientRequestWrapper(final HttpClientRequest httpRequest, final String host) {
        this.httpRequest = Objects.requireNonNull(httpRequest, "httpRequest");
        this.host = host;
    }

    @Override
    public String getDestinationId() {
        if (this.host != null) {
            return this.host;
        }
        return "Unknown";
    }

    @Override
    public String getUrl() {
        return getHttpUrl(httpRequest.getHost(), httpRequest.getPort(), httpRequest.getURI());
    }

    private static String getHttpUrl(final String host, final int port, final String uri) {
        if (host == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        sb.append(host);
        // if port is default port number.
        sb.append(':').append(port);
        if (uri != null) {
            sb.append(uri);
        }
        return sb.toString();
    }
}
