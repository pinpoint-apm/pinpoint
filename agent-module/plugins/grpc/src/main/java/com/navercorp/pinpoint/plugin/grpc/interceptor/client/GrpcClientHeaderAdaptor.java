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

package com.navercorp.pinpoint.plugin.grpc.interceptor.client;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import io.grpc.Metadata;

/**
 * @author Taejin Koo
 */
class GrpcClientHeaderAdaptor implements ClientHeaderAdaptor<Metadata> {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(Metadata header, String name, String value) {
        try {
            final Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
            header.put(key, value);
            if (isDebug) {
                logger.debug("Set header {}={}", name, value);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getHeader(Metadata header, String name) {
        try {
            final Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
            final String value = header.get(key);
            if (value != null) {
                return value;
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    @Override
    public boolean contains(Metadata header, String name) {
        try {
            final Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
            return header.containsKey(key);
        } catch (Exception ignored) {
        }

        return false;
    }
}
