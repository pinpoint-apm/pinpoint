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

package com.navercorp.pinpoint.common.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * @author koo.taejin
 */
public final class NetUtils {

    public static final String LOOPBACK_ADDRESS_V4 = "127.0.0.1";

    private NetUtils() {
    }

    public static List<InetSocketAddress> toInetSocketAddressLIst(List<String> addressList) {
        List<InetSocketAddress> inetSocketAddressList = new ArrayList<InetSocketAddress>();

        for (String address : addressList) {
            InetSocketAddress inetSocketAddress = toInetSocketAddress(address);
            if (inetSocketAddress != null) {
                inetSocketAddressList.add(inetSocketAddress);
            }
        }

        return inetSocketAddressList;
    }

    public static InetSocketAddress toInetSocketAddress(String address) {
        try {
            URI uri = new URI("pinpoint://" + address);

            return new InetSocketAddress(uri.getHost(), uri.getPort());
        } catch (URISyntaxException ignore) {
            // skip
        }

        return null;
    }

    public static String getLocalV4Ip() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String localIp = localHost.getHostAddress();
            if (validationIpV4FormatAddress(localIp)) {
                return localIp;
            }
        } catch (UnknownHostException ignore) {
            // skip
        }
        return LOOPBACK_ADDRESS_V4;
    }

    /**
     * Returns a list of ip addresses on this machine that is accessible from a remote source.
     * If no network interfaces can be found on this machine, returns an empty List.
     */
    public static List<String> getLocalV4IpList() {
        List<String> result = new ArrayList<String>();

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ignore) {
            // skip
        }

        if (interfaces == null) {
            return Collections.emptyList();
        }

        while (interfaces.hasMoreElements()) {
            NetworkInterface current = interfaces.nextElement();
            if (isSkipIp(current)) {
                continue;
            }

            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (address.isLoopbackAddress() || !(address instanceof Inet4Address)) {
                    continue;
                }

                if (validationIpV4FormatAddress(address.getHostAddress())) {
                    result.add(address.getHostAddress());
                }
            }
        }

        return result;
    }

    private static boolean isSkipIp(NetworkInterface networkInterface) {
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


    public static boolean validationIpPortV4FormatAddress(String address) {
        try {
            int splitIndex = address.indexOf(':');

            if (splitIndex == -1 || splitIndex + 1 >= address.length()) {
                return false;
            }

            String ip = address.substring(0, splitIndex);

            if (!validationIpV4FormatAddress(ip)) {
                return false;
            }

            String port = address.substring(splitIndex + 1, address.length());
            if (Integer.parseInt(port) > 65535) {
                return false;
            }

            return true;
        } catch (Exception ignore) {
            //skip
        }

        return false;
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

}
