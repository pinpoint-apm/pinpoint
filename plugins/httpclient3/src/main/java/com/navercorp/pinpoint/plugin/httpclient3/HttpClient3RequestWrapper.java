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

package com.navercorp.pinpoint.plugin.httpclient3;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpConstants;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;

/**
 * @author jaehong.kim
 */
public class HttpClient3RequestWrapper implements ClientRequestWrapper {
    private static final int SKIP_DEFAULT_PORT = -1;
    private static final int MAX_READ_SIZE = 1024;

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    final HttpMethod httpMethod;
    final HttpConnection httpConnection;

    public HttpClient3RequestWrapper(final HttpMethod httpMethod, final HttpConnection httpConnection) {
        this.httpMethod = Assert.requireNonNull(httpMethod, "httpMethod must not be null");
        this.httpConnection = httpConnection;
    }

    @Override
    public void setHeader(final String name, final String value) {
        this.httpMethod.setRequestHeader(name, value);
        if (isDebug) {
            logger.debug("Set header {}={}", name, value);
        }
    }

    @Override
    public String getHost() {
        try {
            final URI uri = this.httpMethod.getURI();
            // if uri have schema
            if (uri.isAbsoluteURI()) {
                return getEndpoint(uri.getHost(), uri.getPort());
            }
            if (this.httpConnection != null) {
                final String host = this.httpConnection.getHost();
                final int port = getPort(this.httpConnection);
                return getEndpoint(host, port);
            }
        } catch (Exception e) {
            if (isDebug) {
                logger.debug("Failed to get host. httpMethod={}", this.httpMethod, e);
            }
        }
        return null;
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

    private static String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "Unknown";
        }
        return HostAndPort.toHostAndPortString(host, HostAndPort.getPortOrNoPort(port));
    }

    private static int getPort(final HttpConnection httpConnection) {
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

    @Override
    public String getEntityValue() {
        if (httpMethod instanceof EntityEnclosingMethod) {
            final EntityEnclosingMethod entityEnclosingMethod = (EntityEnclosingMethod) httpMethod;
            final RequestEntity entity = entityEnclosingMethod.getRequestEntity();
            if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                try {
                    String entityValue;
                    String charSet = entityEnclosingMethod.getRequestCharSet();
                    if (StringUtils.isEmpty(charSet)) {
                        charSet = HttpConstants.DEFAULT_CONTENT_CHARSET;
                    }
                    if (entity instanceof ByteArrayRequestEntity || entity instanceof StringRequestEntity) {
                        entityValue = entityUtilsToString(entity, charSet);
                    } else {
                        entityValue = entity.getClass() + " (ContentType:" + entity.getContentType() + ")";
                    }
                    return entityValue;
                } catch (Exception e) {
                    if (isDebug) {
                        logger.debug("Failed to get entity. httpMethod={}", this.httpMethod, e);
                    }
                }
            }
        }
        return null;
    }

    private static String entityUtilsToString(final RequestEntity entity, final String charSet) throws Exception {
        final FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(MAX_READ_SIZE);
        entity.writeRequest(outStream);
        final String entityValue = outStream.toString(charSet);
        if (entity.getContentLength() > MAX_READ_SIZE) {
            StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append(" (HTTP entity is large. length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }

        return entityValue;
    }


    @Override
    public String getCookieValue() {
        final org.apache.commons.httpclient.Header cookie = httpMethod.getRequestHeader("Cookie");
        if (cookie != null) {
            final String value = cookie.getValue();
            if (StringUtils.hasLength(value)) {
                return value;
            }
        }
        return null;
    }
}