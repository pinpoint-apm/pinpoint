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

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.URI;
import java.net.URISyntaxException;

public class UriUtils {

    public static String path(final String uri) {
        if (uri == null) {
            return null;
        }

        String path = uri;
        int index = path.indexOf('?');
        if (index > -1) {
            path = path.substring(0, index);
        }
        index = path.indexOf('#');
        if (index > -1) {
            path = path.substring(0, index);
        }

        return path;
    }

    public static String host(String uri) {
        if (uri == null) {
            return null;
        }
        try {
            URI u = new URI(uri);
            String host = u.getHost();
            int port = u.getPort();
            return HostAndPort.toHostAndPortString(host, port);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String params(final String uri) {
        if (uri == null) {
            return null;
        }

        String params = uri;
        int index = params.indexOf('?');
        if (index == -1) {
            return null;
        }
        params = params.substring(index + 1);
        index = params.indexOf('#');
        if (index > -1) {
            params = params.substring(0, index);
        }

        return params;
    }
}
