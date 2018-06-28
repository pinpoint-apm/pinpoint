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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.request.body.multipart.ByteArrayPart;
import org.asynchttpclient.request.body.multipart.FilePart;
import org.asynchttpclient.request.body.multipart.Part;
import org.asynchttpclient.request.body.multipart.StringPart;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class NingAsyncHttpClientRequestWrapperV2 implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private static final int MAX_READ_SIZE = 1024;
    private final Request httpRequest;

    public NingAsyncHttpClientRequestWrapperV2(final Request httpRequest) {
        this.httpRequest = Assert.requireNonNull(httpRequest, "httpRequest must not be null");
    }

    @Override
    public void setHeader(final String name, final String value) {
        final HttpHeaders httpRequestHeaders = httpRequest.getHeaders();
        if (httpRequestHeaders != null) {
            httpRequestHeaders.set(name, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        }
    }

    @Override
    public String getHost() {
        return EndPointUtils.getEndPoint(httpRequest.getUrl(), null);
    }

    @Override
    public String getDestinationId() {
        return EndPointUtils.getEndPoint(httpRequest.getUrl(), "Unknown");
    }

    @Override
    public String getUrl() {
        return httpRequest.getUrl();
    }

    @Override
    public String getEntityValue() {
        final String stringData = httpRequest.getStringData();
        if (stringData != null) {
            return stringData;
        }

        final byte[] byteData = httpRequest.getByteData();
        if (byteData != null) {
            return "BYTE_DATA";
        }

        final InputStream streamData = httpRequest.getStreamData();
        if (streamData != null) {
            return "STREAM_DATA";
        }

        List<Part> parts = httpRequest.getBodyParts();
        // bug fix : parts != null && ****!parts.isEmpty()
        if (CollectionUtils.isNotEmpty(parts)) {
            StringBuilder sb = new StringBuilder();
            Iterator<Part> iterator = parts.iterator();
            while (iterator.hasNext()) {
                Part part = iterator.next();
                if (part instanceof ByteArrayPart) {
                    ByteArrayPart p = (ByteArrayPart) part;
                    sb.append(part.getName());
                    sb.append("=BYTE_ARRAY_");
                    sb.append(p.getBytes().length);
                } else if (part instanceof FilePart) {
                    FilePart p = (FilePart) part;
                    sb.append(part.getName());
                    sb.append("=FILE_");
                    sb.append(p.getContentType());
                } else if (part instanceof StringPart) {
                    StringPart p = (StringPart) part;
                    sb.append(part.getName());
                    sb.append("=STRING");
                }

                if (sb.length() >= MAX_READ_SIZE) {
                    break;
                }

                if (iterator.hasNext()) {
                    sb.append(',');
                }
            }
            return sb.toString();
        }

        return null;
    }

    @Override
    public String getCookieValue() {
        List<Cookie> cookies = httpRequest.getCookies();
        if (cookies.isEmpty()) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            sb.append(cookie.getName()).append('=').append(cookie.getValue());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }
}