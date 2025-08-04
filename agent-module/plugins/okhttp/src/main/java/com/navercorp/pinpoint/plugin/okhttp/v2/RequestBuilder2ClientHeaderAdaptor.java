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

package com.navercorp.pinpoint.plugin.okhttp.v2;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Request;

/**
 * @author Woonduk Kang(emeroad)
 */
public class RequestBuilder2ClientHeaderAdaptor implements ClientHeaderAdaptor<Request.Builder> {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(Request.Builder builder, String name, String value) {
        try {
            builder.header(name, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getHeader(Request.Builder header, String name) {
        try {
            if (header instanceof HeadersBuilder) {
                final Headers.Builder builder = ((HeadersBuilder) header)._$PINPOINT$_getHeadersBuilder();
                if (builder != null) {
                    final String value = builder.get(name);
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
    public boolean contains(Request.Builder header, String name) {
        try {
            if (header instanceof HeadersBuilder) {
                final Headers.Builder builder = ((HeadersBuilder) header)._$PINPOINT$_getHeadersBuilder();
                if (builder != null) {
                    final String value = builder.get(name);
                    if (value != null) {
                        return true;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return false;
    }
}
