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

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import reactor.netty.http.client.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientRequestHeaderAdaptor implements ClientHeaderAdaptor<HttpClientRequest> {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(final HttpClientRequest request, final String name, final String value) {
        try {
            if (request != null) {
                request.header(name, value);
                if (isDebug) {
                    logger.debug("Set header {}={}", name, value);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getHeader(HttpClientRequest header, String name) {
        try {
            if (header != null) {
                String value = header.requestHeaders().get(name);
                if (value != null) {
                    return value;
                }
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    @Override
    public boolean contains(HttpClientRequest header, String name) {
        try {
            if (header != null) {
                return header.requestHeaders().contains(name);
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}