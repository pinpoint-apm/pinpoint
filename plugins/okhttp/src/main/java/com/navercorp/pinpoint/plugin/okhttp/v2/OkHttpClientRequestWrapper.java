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

package com.navercorp.pinpoint.plugin.okhttp.v2;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestWrapper;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import com.squareup.okhttp.Request;

import java.net.URL;

/**
 * @author jaehong.kim
 */
public class OkHttpClientRequestWrapper implements ClientRequestWrapper {
    private final Request request;

    public OkHttpClientRequestWrapper(final Request request) {
        this.request = Assert.requireNonNull(request, "request must not be null");
    }

    @Override
    public void setHeader(final String name, final String value) {
        throw new UnsupportedOperationException("Must be used only in the HttpEngineSendRequestMethodInterceptor class");
    }

    @Override
    public String getHost() {
        return null;
    }

    @Override
    public String getDestinationId() {
        final URL httpUrl = request.url();
        if (httpUrl == null || httpUrl.getHost() == null) {
            return "Unknown";
        }
        final int port = EndPointUtils.getPort(httpUrl.getPort(), httpUrl.getDefaultPort());
        return HostAndPort.toHostAndPortString(httpUrl.getHost(), port);
    }

    @Override
    public String getUrl() {
        return this.request.urlString();
    }

    @Override
    public String getEntityValue() {
        return null;
    }

    @Override
    public String getCookieValue() {
        for (String cookie : request.headers("Cookie")) {
            if (StringUtils.hasLength(cookie)) {
                return cookie;
            }
        }
        return null;
    }
}