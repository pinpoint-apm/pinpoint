/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;
import io.netty.handler.codec.http.cookie.Cookie;
import reactor.netty.http.client.HttpClientRequest;

import java.util.Map;
import java.util.Set;

/**
 * @author jaehong.kim
 */
public class HttpClientRequestCookieExtractor implements CookieExtractor<HttpClientRequest> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public String getCookie(final HttpClientRequest request) {
        if (request != null && request.cookies() != null) {
            final StringBuilder sb = new StringBuilder();
            for (Map.Entry<CharSequence, Set<Cookie>> entry : request.cookies().entrySet()) {
                boolean repeated = false;
                for (Cookie httpCookie : entry.getValue()) {
                    if (repeated) {
                        sb.append(',');
                    }
                    sb.append(httpCookie.name());
                    sb.append('=');
                    sb.append(httpCookie.value());
                    repeated = true;
                }
            }
            if (isDebug) {
                logger.debug("Cookie={}", sb.toString());
            }
            return sb.toString();
        }
        return null;
    }
}