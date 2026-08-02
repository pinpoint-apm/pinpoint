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

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;

public class KtorClientRequestAdaptor implements ClientRequestAdaptor<Object> {
    private static final String UNKNOWN = "Unknown";

    @Override
    public String getDestinationId(Object request) {
        Object url = getUrlObject(request);
        if (url == null) {
            return UNKNOWN;
        }

        Object hostObject = invoke(url, "getHost", new Class<?>[]{});
        if (!(hostObject instanceof String)) {
            return UNKNOWN;
        }

        String host = (String) hostObject;
        if (host.isEmpty() || host.trim().isEmpty()) {
            return UNKNOWN;
        }

        int port = resolvePort(url);
        if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
            return "[" + host + "]:" + port;
        }
        return host + ":" + port;
    }

    @Override
    public String getUrl(Object request) {
        Object url = getUrlObject(request);
        if (url == null) {
            return null;
        }

        Object urlString = invoke(url, "buildString", new Class<?>[]{});
        return urlString instanceof String ? (String) urlString : null;
    }

    private int resolvePort(Object url) {
        Object portObject = invoke(url, "getPort", new Class<?>[]{});
        int port = portObject instanceof Number ? ((Number) portObject).intValue() : 0;
        if (port > 0) {
            return port;
        }

        Object protocol = invoke(url, "getProtocolOrNull", new Class<?>[]{});
        if (protocol != null) {
            Object defaultPort = invoke(protocol, "getDefaultPort", new Class<?>[]{});
            if (defaultPort instanceof Number) {
                return ((Number) defaultPort).intValue();
            }
        }
        return 0;
    }

    private Object getUrlObject(Object request) {
        if (request == null) {
            return null;
        }
        return invoke(request, "getUrl", new Class<?>[]{});
    }

    private Object invoke(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return target.getClass().getMethod(methodName, parameterTypes).invoke(target, args);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
