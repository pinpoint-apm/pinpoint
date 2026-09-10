/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ClientHttpRequest;

/**
 * Only {@link HttpHeaders} methods whose signature is identical in Spring 5, 6 and 7 are used here
 * ({@code getFirst(String)}, {@code set(String, String)}). Spring Framework 7.0 dropped the
 * {@code MultiValueMap} implementation from {@code HttpHeaders}, so the {@code Map} methods this class
 * was compiled against on 5.3 ({@code containsKey(Object)}, {@code keySet()}, {@code get(Object)})
 * no longer exist at runtime and fail with {@code NoSuchMethodError}.
 */
public class ClientHttpRequestClientHeaderAdaptor implements ClientHeaderAdaptor<ClientHttpRequest> {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(final ClientHttpRequest request, final String name, final String value) {
        try {
            if (request != null) {
                final HttpHeaders headers = request.getHeaders();
                if (headers != null) {
                    headers.set(name, value);
                    if (isDebug) {
                        logger.debug("Set header {}={}", name, value);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getHeader(ClientHttpRequest header, String name) {
        try {
            if (header != null) {
                final HttpHeaders headers = header.getHeaders();
                if (headers != null) {
                    String value = headers.getFirst(name);
                    if (value != null) {
                        return value;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    @Override
    public boolean contains(ClientHttpRequest header, String name) {
        try {
            if (header != null) {
                final HttpHeaders headers = header.getHeaders();
                if (headers != null) {
                    return headers.getFirst(name) != null;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
