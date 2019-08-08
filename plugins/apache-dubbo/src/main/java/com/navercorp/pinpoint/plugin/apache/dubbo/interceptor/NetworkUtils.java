/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.apache.dubbo.interceptor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author K
 */
public final class NetworkUtils {
    private static final PLogger logger = PLoggerFactory.getLogger(NetworkUtils.class);

    private static volatile String LOCAL_ADDRESS_CACHE;

    private NetworkUtils() {
    }

    public static String getLocalHost() {
        if (LOCAL_ADDRESS_CACHE != null) {
            // expire time?
            return LOCAL_ADDRESS_CACHE;
        }
        String localHost = getLocalHost0();
        LOCAL_ADDRESS_CACHE = localHost;
        return localHost;
    }

    private static String getLocalHost0() {
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                String name = netInterface.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address && !ip.isLoopbackAddress()) {
                            final String hostAddress = ip.getHostAddress();
                            if (hostAddress.indexOf(':') == -1) {
                                // TODO We do not seem to understand this problem exactly.
                                logger.info("APACHE DUBBO localAddress cache:{}", hostAddress);
                                return hostAddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error("failed to get local host", e);
            return null;
        }
        return null;
    }
}
