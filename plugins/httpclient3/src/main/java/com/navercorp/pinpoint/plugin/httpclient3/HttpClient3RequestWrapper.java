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

package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * @author jaehong.kim
 */
public class HttpClient3RequestWrapper implements ClientRequestWrapper {
    private static final int SKIP_DEFAULT_PORT = -1;


    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpMethod httpMethod;
    private final HttpConnection httpConnection;

    public HttpClient3RequestWrapper(final HttpMethod httpMethod, final HttpConnection httpConnection) {
        this.httpMethod = Assert.requireNonNull(httpMethod, "httpMethod");
        this.httpConnection = httpConnection;
    }


    @Override
    public String getDestinationId() {
        try {
            final URI uri = this.httpMethod.getURI();
            // if uri have schema or not found HttpConnection argument.
            if (uri.isAbsoluteURI() || this.httpConnection == null) {
                return getEndpoint(uri.getHost(), uri.getPort());
            }
            final String host = this.httpConnection.getHost();
            final int port = getPort(this.httpConnection);
            return getEndpoint(host, port);
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("Failed to get destinationId. httpMethod={}", this.httpMethod, e);
            }
        }
        return "unknown";
    }

    @Override
    public String getUrl() {
        try {
            final URI uri = this.httpMethod.getURI();
            // if uri have schema or not found HttpConnection argument.
            if (uri.isAbsoluteURI() || this.httpConnection == null) {
                return uri.getURI();
            }
            final String host = this.httpConnection.getHost();
            final int port = getPort(this.httpConnection);
            return getHttpUrl(host, port, uri, this.httpConnection);
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("Failed to get url. httpMethod={}", this.httpMethod, e);
            }
        }
        return null;
    }

    public static String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "Unknown";
        }
        return HostAndPort.toHostAndPortString(host, HostAndPort.getPortOrNoPort(port));
    }

    public static int getPort(final HttpConnection httpConnection) {
        if (httpConnection == null) {
            return SKIP_DEFAULT_PORT;
        }
        final int port = httpConnection.getPort();
        final Protocol protocol = httpConnection.getProtocol();
        // if port is default port number.
        if (protocol != null && port == protocol.getDefaultPort()) {
            // skip
            return SKIP_DEFAULT_PORT;
        }
        return port;
    }

    private static String getHttpUrl(final String host, final int port, final URI uri, final HttpConnection httpConnection) throws URIException {
        final Protocol protocol = httpConnection.getProtocol();
        if (protocol == null) {
            return uri.getURI();
        }
        final StringBuilder sb = new StringBuilder();
        final String scheme = protocol.getScheme();
        sb.append(scheme).append("://");
        sb.append(host);
        // if port is default port number.
        if (port != SKIP_DEFAULT_PORT) {
            sb.append(':').append(port);
        }
        sb.append(uri.getURI());
        return sb.toString();
    }

}