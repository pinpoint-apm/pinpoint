/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.CookieExtractor;

import org.springframework.http.HttpCookie;
import org.springframework.http.client.reactive.ClientHttpRequest;

import java.util.List;
import java.util.Map;

/**
 * @author jaehong.kim
 */
public class ClientHttpRequestCookieExtractor implements CookieExtractor<ClientHttpRequest> {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public String getCookie(final ClientHttpRequest request) {
        if (request != null && request.getCookies() != null) {
            final StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<HttpCookie>> entry : request.getCookies().entrySet()) {
                boolean repeated = false;
                for (HttpCookie httpCookie : entry.getValue()) {
                    if (repeated) {
                        sb.append(',');
                    }
                    sb.append(httpCookie.getName());
                    sb.append('=');
                    sb.append(httpCookie.getValue());
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