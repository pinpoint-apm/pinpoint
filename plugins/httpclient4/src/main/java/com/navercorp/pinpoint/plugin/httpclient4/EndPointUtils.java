/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.httpclient4;

import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;

/**
 * @author Woonduk Kang(emeroad)
 */
public class EndPointUtils {


    public static String getHostAndPort(HttpRoute route) {
        final HttpHost proxyHost = route.getProxyHost();
        if (proxyHost != null) {
            final String hostName = proxyHost.getHostName();
            final int port = proxyHost.getPort();
            if (port > 0) {
                return hostAndPort(hostName, port);
            }
            return hostName;
        } else {
            final HttpHost targetHost = route.getTargetHost();
            if (targetHost != null) {
                final String hostName = targetHost.getHostName();
                final int port = targetHost.getPort();
                if (port > 0) {
                    return hostAndPort(hostName, targetHost.getPort());
                }
                return hostName;
            }
        }
        return "";
    }

    public static String hostAndPort(String host, int port) {
        final StringBuilder sb = new StringBuilder();
        sb.append(host);
        sb.append(':');
        sb.append(port);
        return sb.toString();
    }
}
