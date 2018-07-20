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

package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author jaehong.kim
 */
public class JdkHttpClientRequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    final HttpURLConnection httpURLConnection;

    public JdkHttpClientRequestWrapper(final HttpURLConnection httpURLConnection) {
        this.httpURLConnection = Assert.requireNonNull(httpURLConnection, "httpURLConnection must not be null");
    }

    @Override
    public void setHeader(final String name, final String value) {
        this.httpURLConnection.setRequestProperty(name, value);
        if (isDebug) {
            logger.debug("Set header {}={}", name, value);
        }
    }

    @Override
    public String getHost() {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            final String host = url.getHost();
            final int port = url.getPort();
            if (host != null) {
                return getEndpoint(host, port);
            }
        }
        return null;
    }

    @Override
    public String getDestinationId() {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            final String host = url.getHost();
            final int port = url.getPort();
            return getEndpoint(host, port);
        }
        return "Unknown";
    }

    private String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "Unknown";
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
    public String getUrl() {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            return url.toString();
        }
        return null;
    }

    @Override
    public String getEntityValue() {
        return null;
    }

    @Override
    public String getCookieValue() {
        return null;
    }
}
