/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.httpclient;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.util.EntityExtractor;
import jdk.internal.net.http.HttpRequestImpl;

import java.net.http.HttpRequest;
import java.util.Optional;

public class JdkHttpClientEntityExtractor implements EntityExtractor<HttpRequestImpl> {

    private static final int MAX_READ_SIZE = 1024;

    public static final EntityExtractor<HttpRequestImpl> INSTANCE = new JdkHttpClientEntityExtractor();

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public String getEntity(HttpRequestImpl httpMethod) {
        final Optional<HttpRequest.BodyPublisher> bodyPublisher = httpMethod.bodyPublisher();
        if (isDebug) {
            logger.debug("Get entity. request={}, bodyPublisher={}", httpMethod, bodyPublisher);
        }
        if (bodyPublisher != null && bodyPublisher.isPresent()) {
            return "ContentLength: " + bodyPublisher.get().contentLength();
        }
        return null;
    }
}