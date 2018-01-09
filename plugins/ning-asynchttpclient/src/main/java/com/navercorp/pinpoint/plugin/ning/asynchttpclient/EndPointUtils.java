/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ning.asynchttpclient;

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;


/**
 * @author Woonduk Kang(emeroad)
 */
public class EndPointUtils {

    public static String getEndPoint(final String requestUrl, final String defaultValue) {
        if (requestUrl == null) {
            return defaultValue;
        }

        final String urlString = requestUrl.trim();
        if (urlString.isEmpty()) {
            return defaultValue;
        }

        final String hostAndPort = parseHostAndPort(urlString);
        if (hostAndPort == null) {
            // pass original.
            return requestUrl;
        }

        final int portPosition = hostAndPort.indexOf(':');
        if (portPosition != -1) {
            int port = HostAndPort.NO_PORT;
            try {
                port = Integer.parseInt(hostAndPort.substring(portPosition + 1));
            } catch (NumberFormatException nfe) {
                // pass
                return hostAndPort;
            }
            final String host = hostAndPort.substring(0, portPosition);
            return HostAndPort.toHostAndPortString(host, port);
        } else {
            return hostAndPort;
        }
    }

    // for http:// and https://
    private static String parseHostAndPort(final String requestUrl) {
        int length = requestUrl.length();
        int startPosition = 0;

        // skip protocol
        int protocolPosition = requestUrl.indexOf("://");
        if (protocolPosition != -1) {
            // http:// or https://
            startPosition = protocolPosition + 3;
        } else {
            // unexpected protocol.
            return null;
        }

        char c;
        int endPoisition = length;
        for (int i = startPosition; i < length; i++) {
            c = requestUrl.charAt(i);
            if (c == '/' || c == '?') {
                endPoisition = i;
                break;
            }
        }

        return requestUrl.substring(startPosition, endPoisition);
    }
}