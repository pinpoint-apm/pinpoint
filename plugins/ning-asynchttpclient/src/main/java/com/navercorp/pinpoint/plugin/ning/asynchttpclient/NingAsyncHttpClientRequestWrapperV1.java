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
import com.ning.http.client.FluentCaseInsensitiveStringsMap;
import com.ning.http.client.Request;
import com.ning.http.client.cookie.Cookie;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class NingAsyncHttpClientRequestWrapperV1 implements ClientRequestWrapper {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Request request;

    public NingAsyncHttpClientRequestWrapperV1(final Request request) {
        this.request = Assert.requireNonNull(request, "request must not be null");
    }

    @Override
    public void setHeader(final String name, final String value) {
        final FluentCaseInsensitiveStringsMap httpRequestHeaders = this.request.getHeaders();
        final List<String> valueList = new ArrayList<String>();
        valueList.add(value);
        httpRequestHeaders.put(name, valueList);
        if (isDebug) {
            logger.debug("Set header {}={}", name, value);
        }
    }

    @Override
    public String getHost() {
        return EndPointUtils.getEndPoint(this.request.getUrl(), null);
    }

    @Override
    public String getDestinationId() {
        return EndPointUtils.getEndPoint(this.request.getUrl(), "Unknown");
    }

    @Override
    public String getUrl() {
        return request.getUrl();
    }

    @Override
    public String getEntityValue() {
        final String stringData = request.getStringData();
        if (stringData != null) {
            return stringData;
        }

        final byte[] byteData = request.getByteData();
        if (byteData != null) {
            return "BYTE_DATA";
        }

        final InputStream streamData = request.getStreamData();
        if (streamData != null) {
            return "STREAM_DATA";
        }

        return null;
    }

    @Override
    public String getCookieValue() {
        final Collection<Cookie> cookies = request.getCookies();
        if (cookies.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            final Cookie cookie = iterator.next();
            sb.append(cookie.getName()).append('=').append(cookie.getValue());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }
}