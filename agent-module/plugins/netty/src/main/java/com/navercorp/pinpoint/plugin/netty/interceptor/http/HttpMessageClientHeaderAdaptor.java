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

package com.navercorp.pinpoint.plugin.netty.interceptor.http;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HttpMessageClientHeaderAdaptor implements ClientHeaderAdaptor<HttpMessage> {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(HttpMessage httpMessage, String name, String value) {
        try {
            final HttpHeaders headers = httpMessage.headers();
            if (headers != null && !headers.contains(name)) {
                headers.set(name, value);
                if (isDebug) {
                    logger.debug("Set header {}={}", name, value);
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean contains(HttpMessage header, String name) {
        try {
            final HttpHeaders headers = header.headers();
            if (headers != null) {
                return headers.contains(name);
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
