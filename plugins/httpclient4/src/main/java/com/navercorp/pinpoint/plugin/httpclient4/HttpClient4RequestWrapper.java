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

package com.navercorp.pinpoint.plugin.httpclient4;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.bootstrap.util.FixedByteArrayOutputStream;
import com.navercorp.pinpoint.common.Charsets;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;

import java.io.IOException;

/**
 * @author jaehong.kim
 */
public class HttpClient4RequestWrapper implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final HttpRequest httpRequest;
    private final String hostName;
    private final int port;

    public HttpClient4RequestWrapper(final HttpRequest httpRequest, final String hostName, final int port) {
        this.httpRequest = Assert.requireNonNull(httpRequest, "httpRequest must not be null");
        this.hostName = hostName;
        this.port = port;
    }

    @Override
    public void setHeader(final String name, final String value) {
        this.httpRequest.setHeader(name, value);
        if (isDebug) {
            logger.debug("Set header {}={}", name, value);
        }
    }

    @Override
    public String getHost() {
        if (this.hostName != null) {
            return getEndpoint(this.hostName, this.port);
        }
        return null;
    }

    @Override
    public String getDestinationId() {
        return getEndpoint(this.hostName, this.port);
    }

    private String getEndpoint(final String host, final int port) {
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
        if (this.httpRequest.getRequestLine() != null) {
            return httpRequest.getRequestLine().getUri();
        }
        return null;
    }

    @Override
    public String getEntityValue() {
        if (this.httpRequest instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) this.httpRequest;
            try {
                final HttpEntity entity = entityRequest.getEntity();
                if (entity != null && entity.isRepeatable() && entity.getContentLength() > 0) {
                    return entityUtilsToString(entity, Charsets.UTF_8_NAME, 1024);
                }
            } catch (Exception e) {
                logger.debug("Failed to get entity. httpRequest={}", this.httpRequest, e);
            }
        }
        return null;
    }

    /**
     * copy: EntityUtils Get the entity content as a String, using the provided default character set if none is found in the entity. If defaultCharset is null, the default "ISO-8859-1" is used.
     *
     * @param entity         must not be null
     * @param defaultCharset character set to be applied if none found in the entity
     * @return the entity content as a String. May be null if {@link HttpEntity#getContent()} is null.
     * @throws ParseException           if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null or if content length > Integer.MAX_VALUE
     * @throws IOException              if an error occurs reading the input stream
     */
    @SuppressWarnings("deprecation")
    private static String entityUtilsToString(final HttpEntity entity, final String defaultCharset, final int maxLength) throws Exception {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity must not be null");
        }
        if (entity.getContentLength() > Integer.MAX_VALUE) {
            return "HTTP entity is too large to be buffered in memory length:" + entity.getContentLength();
        }
        if (entity.getContentType().getValue().startsWith("multipart/form-data")) {
            return "content type is multipart/form-data. content length:" + entity.getContentLength();
        }

        String charset = getContentCharSet(entity);
        if (charset == null) {
            charset = defaultCharset;
        }
        if (charset == null) {
            charset = HTTP.DEFAULT_CONTENT_CHARSET;
        }

        final FixedByteArrayOutputStream outStream = new FixedByteArrayOutputStream(maxLength);
        entity.writeTo(outStream);
        final String entityValue = outStream.toString(charset);
        if (entity.getContentLength() > maxLength) {
            final StringBuilder sb = new StringBuilder();
            sb.append(entityValue);
            sb.append("HTTP entity large length: ");
            sb.append(entity.getContentLength());
            sb.append(" )");
            return sb.toString();
        }

        return entityValue;
    }

    /**
     * copy: EntityUtils Obtains character set of the entity, if known.
     *
     * @param entity must not be null
     * @return the character set, or null if not found
     * @throws ParseException           if header elements cannot be parsed
     * @throws IllegalArgumentException if entity is null
     */
    public static String getContentCharSet(final HttpEntity entity) throws ParseException {
        if (entity == null) {
            throw new IllegalArgumentException("HTTP entity must not be null");
        }
        String charset = null;
        if (entity.getContentType() != null) {
            HeaderElement values[] = entity.getContentType().getElements();
            if (values.length > 0) {
                NameValuePair param = values[0].getParameterByName("charset");
                if (param != null) {
                    charset = param.getValue();
                }
            }
        }
        return charset;
    }

    @Override
    public String getCookieValue() {
        final org.apache.http.Header[] cookies = this.httpRequest.getHeaders("Cookie");
        for (org.apache.http.Header header : cookies) {
            final String value = header.getValue();
            if (StringUtils.hasLength(value)) {
                return value;
            }
        }
        return null;
    }
}