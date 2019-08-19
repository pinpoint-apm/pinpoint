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

package com.navercorp.pinpoint.plugin.jdk.http;

import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author jaehong.kim
 */
public class JdkHttpClientRequestAdaptor implements ClientRequestAdaptor<HttpURLConnection> {


    public JdkHttpClientRequestAdaptor() {
    }


    @Override
    public String getDestinationId(HttpURLConnection httpURLConnection) {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            final String host = url.getHost();
            final int port = url.getPort();
            return getEndpoint(host, port);
        }
        return "Unknown";
    }

    public static String getEndpoint(final String host, final int port) {
        if (host == null) {
            return "Unknown";
        }
        if (port < 0) {
            return host;
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }

    @Override
    public String getUrl(HttpURLConnection httpURLConnection) {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            return url.toString();
        }
        return null;
    }

}
