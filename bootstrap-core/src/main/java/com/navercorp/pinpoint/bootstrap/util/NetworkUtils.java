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

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author emeroad
 */
public final class NetworkUtils {

    public static final String ERROR_HOST_NAME = "UNKNOWN-HOST";

    private static final String LOOPBACK_ADDRESS_V4_1 = "127.0.0.1";
    private static final String LOOPBACK_ADDRESS_V4_2 = "127.0.1.1";
    private static final String LOOPBACK_ADDRESS_V6 = "0:0:0:0:0:0:0:1";

    private static final List<String> LOOP_BACK_ADDRESS_LIST;

    static {
        LOOP_BACK_ADDRESS_LIST = new ArrayList<String>(3);
        LOOP_BACK_ADDRESS_LIST.add(LOOPBACK_ADDRESS_V4_1);
        LOOP_BACK_ADDRESS_LIST.add(LOOPBACK_ADDRESS_V4_2);
        LOOP_BACK_ADDRESS_LIST.add(LOOPBACK_ADDRESS_V6);
    }

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

    public static String getRepresentationHostIp() {
        String ip = getHostIp();
        if (!isLoopbackAddress(ip)) {
            return ip;
        }

        List<String> ipList = getHostIpList();
        if (!ipList.isEmpty()) {
            return ipList.get(0);
        }

        return LOOPBACK_ADDRESS_V4_1;
    }

    public static String getHostIp() {
        String hostIp;
        try {
            final InetAddress thisIp = InetAddress.getLocalHost();
            hostIp = thisIp.getHostAddress();
        } catch (UnknownHostException e) {
            CommonLogger logger = getLogger();
            logger.warn(e.getMessage());
            hostIp = LOOPBACK_ADDRESS_V4_1;
        }
        return hostIp;
    }

    public static List<String> getHostIpList() {
        List<String> result = new ArrayList<String>();

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ignore) {
            // skip
        }

        if (interfaces == null) {
            return Collections.EMPTY_LIST;
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            if (isSkipNetworkInterface(current)) {
                continue;
            }

            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress()) {
                    continue;
                }

                String hostAddress = address.getHostAddress();
                if (!isLoopbackAddress(hostAddress)) {
                    result.add(address.getHostAddress());
                }
            }
        }

        return result;
    }

    public static String getHostV4Ip() {
        String hostIp = getHostIp();
        if (validationIpV4FormatAddress(hostIp)) {
            return hostIp;
        }
        return LOOPBACK_ADDRESS_V4_1;
    }

    public static List<String> getHostV4IpList() {
        List<String> hostIpList = getHostIpList();
        List<String> hostV4IpList = new ArrayList<String>(hostIpList.size());
        for (String ip : hostIpList) {
            if (validationIpV4FormatAddress(ip)) {
                hostV4IpList.add(ip);
            }
        }

        return hostV4IpList;
    }

    private static boolean isSkipNetworkInterface(NetworkInterface networkInterface) {
        try {
            if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                return true;
            }
            return false;
        } catch (Exception ignore) {
            // skip
        }
        return true;
    }

    public static boolean isLoopbackAddress(String ip) {
        if (ip == null) {
            return true;
        }
        return LOOP_BACK_ADDRESS_LIST.contains(ip);
    }

    public static boolean validationIpV4FormatAddress(String address) {
        try {
            String[] eachDotAddress = address.split("\\.");
            if (eachDotAddress.length != 4) {
                return false;
            }

            for (String eachAddress : eachDotAddress) {
                if (Integer.parseInt(eachAddress) > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException ignore) {
            // skip
        }

        return false;
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
