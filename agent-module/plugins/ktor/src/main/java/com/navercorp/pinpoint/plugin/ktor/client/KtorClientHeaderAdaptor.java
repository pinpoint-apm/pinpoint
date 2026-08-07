/*
 * Copyright 2026 NAVER Corp.
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


package com.navercorp.pinpoint.plugin.ktor.client;

import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;

public class KtorClientHeaderAdaptor implements ClientHeaderAdaptor<Object> {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    @Override
    public void setHeader(Object request, String name, String value) {
        Object headers = getHeaders(request);
        if (headers == null) {
            return;
        }

        invoke(headers, "set", new Class<?>[]{String.class, String.class}, name, value);
        if (isDebug) {
            logger.debug("Set Ktor client header {}={}", name, value);
        }
    }

    @Override
    public String getHeader(Object request, String name) {
        Object headers = getHeaders(request);
        if (headers == null) {
            return "";
        }

        Object value = invoke(headers, "get", new Class<?>[]{String.class}, name);
        return value == null ? "" : value.toString();
    }

    @Override
    public boolean contains(Object request, String name) {
        Object headers = getHeaders(request);
        if (headers == null) {
            return false;
        }

        Object result = invoke(headers, "contains", new Class<?>[]{String.class}, name);
        return result instanceof Boolean && (Boolean) result;
    }

    private Object getHeaders(Object request) {
        if (request == null) {
            return null;
        }
        return invoke(request, "getHeaders", new Class<?>[]{});
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return target.getClass().getMethod(methodName, parameterTypes).invoke(target, args);
        } catch (Throwable throwable) {
            logger.warn("Failed to access Ktor client headers. {}", throwable.getMessage(), throwable);
            return null;
        }
    }
}
