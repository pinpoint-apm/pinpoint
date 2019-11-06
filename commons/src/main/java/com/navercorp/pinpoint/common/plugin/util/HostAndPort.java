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

package com.navercorp.pinpoint.common.plugin.util;

import com.navercorp.pinpoint.common.util.StringUtils;

/**
 * @author Woonduk Kang(emeroad)
 */
public class HostAndPort {
    public static final int NO_PORT = -1;

// TODO
//    private final String host;
//    private final int port;
//
//    public HostAndPort(String host, int port) {
//        this.host = host;
//        this.port = port;
//    }


    public static int getValidPortOrNoPort(int port) {
        if (!isValidPort(port)) {
            return NO_PORT;
        }
        return port;
    }

    public static int getPortOrNoPort(int port) {
        if (port < 0) {
            return HostAndPort.NO_PORT;
        }
        return port;
    }

    public static boolean isValidPort(int port) {
        return port >= 0 && port <= 65535;
    }

    public static String toHostAndPortString(String host, int port) {
        return toHostAndPortString(host, port, NO_PORT);
    }

    /**
     * This API does not verification for input args.
     */
    public static String toHostAndPortString(String host, int port, int noPort) {
        // don't validation hostName
        // don't validation port range
        if (noPort == port) {
            return host;
        }
        final int hostLength = StringUtils.getLength(host);
        final StringBuilder builder = new StringBuilder(hostLength + 6);
        builder.append(host);
        builder.append(':');
        builder.append(port);
        return builder.toString();
    }

}
