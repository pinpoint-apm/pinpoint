/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.util;

import com.navercorp.pinpoint.common.util.logger.CommonLogger;
import com.navercorp.pinpoint.common.util.logger.StdoutCommonLoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * @author emeroad
 */
public final class NetworkUtils {

    public static final String ERROR_HOST_NAME = "UNKNOWN-HOST";

    private NetworkUtils() {
    }

    public static String getHostName() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            // Try to get machine name from network interface.
            return getMachineName();
        }
    }

    public static String getHostIp() {
        String hostIp;
        try {
            final InetAddress thisIp = InetAddress.getLocalHost();
            hostIp = thisIp.getHostAddress();
        } catch (UnknownHostException e) {
            CommonLogger logger = getLogger();
            logger.warn(e.getMessage());
            hostIp = "127.0.0.1";
        }
        return hostIp;
    }

    private static CommonLogger getLogger() {
        return StdoutCommonLoggerFactory.INSTANCE.getLogger(NetworkUtils.class.getClass().getName());
    }

    @Deprecated
    public static String getMachineName() {
        try {
            Enumeration<NetworkInterface> enet = NetworkInterface.getNetworkInterfaces();
            while (enet.hasMoreElements()) {

                NetworkInterface net = enet.nextElement();
                if (net.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> eaddr = net.getInetAddresses();

                while (eaddr.hasMoreElements()) {
                    InetAddress inet = eaddr.nextElement();

                    final String canonicalHostName = inet.getCanonicalHostName();
                    if (!canonicalHostName.equalsIgnoreCase(inet.getHostAddress())) {
                        return canonicalHostName;
                    }
                }
            }
            return ERROR_HOST_NAME;
        } catch (SocketException e) {
            CommonLogger logger = getLogger();
            logger.warn(e.getMessage());
            return ERROR_HOST_NAME;
        }
    }

    public static String getHostFromURL(final String urlSpec) {
        if (urlSpec == null) {
            return null;
        }
        try {
            final URL url = new URL(urlSpec);

            final String host = url.getHost();
            final int port = url.getPort();

            if (port == -1) {
                return host;
            } else {
                // TODO should we still specify the port number if default port is used?
                return host + ":" + port;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
