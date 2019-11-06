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

import com.navercorp.pinpoint.common.plugin.util.HostAndPort;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
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


    private static final HostAndPortFactory<InetSocketAddress> inetSocketAddressFactory = new HostAndPortFactory<InetSocketAddress>() {
        @Override
        public InetSocketAddress newInstance(String host, int port) {
            if (!HostAndPort.isValidPort(port)) {
                return null;
            }
            return new InetSocketAddress(host, port);
        }
    };

    private NetUtils() {
    }

    public static List<InetSocketAddress> toInetSocketAddressLIst(List<String> addressList) {
        return toHostAndPortLIst(addressList, inetSocketAddressFactory);
    }


    public interface HostAndPortFactory<T> {
        T newInstance(String host, int port);
    }

    public static <T> List<T> toHostAndPortLIst(List<String> addressList, HostAndPortFactory<T> hostAndPortFactory) {
        if (CollectionUtils.isEmpty(addressList)) {
            return Collections.emptyList();
        }
        final List<T> hostAndPortList = new ArrayList<T>(addressList.size());
        for (String address : addressList) {
            final T hostAndPort = parseHostAndPort(address, hostAndPortFactory);
            if (hostAndPort != null) {
                hostAndPortList.add(hostAndPort);
            }
        }
        return hostAndPortList;
    }


    public static InetSocketAddress toInetSocketAddress(String address) {
        return parseHostAndPort(address, inetSocketAddressFactory);
    }

    public  static <T> T parseHostAndPort(String address, HostAndPortFactory<T> hostAndPortFactory) {
        if (StringUtils.isEmpty(address)) {
            return null;
        }
        Assert.requireNonNull(hostAndPortFactory, "hostAndPortFactory");

        final int hostIndex = address.indexOf(':');
        if (hostIndex == -1) {
            return null;
        }
        final String host = address.substring(0, hostIndex);
        final String portString = address.substring(hostIndex +1, address.length());
        final int port = parseInteger(portString, HostAndPort.NO_PORT);
        return hostAndPortFactory.newInstance(host, port);
    }

    /**
     * TODO duplicate code
     * com.navercorp.pinpoint.bootstrap.util.NumberUtils.parseInteger();
     */
    private static int parseInteger(String str, int defaultInt) {
        if (str == null) {
            return defaultInt;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return defaultInt;
        }
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

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException ignore) {
            // skip
        }

        if (interfaces == null) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>();
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

        final int splitIndex = address.indexOf(':');
        if (splitIndex == -1 || splitIndex + 1 >= address.length()) {
            return false;
        }

        final String ip = address.substring(0, splitIndex);
        if (!validationIpV4FormatAddress(ip)) {
            return false;
        }

        final String portString = address.substring(splitIndex + 1, address.length());
        final int port = parseInteger(portString, HostAndPort.NO_PORT);
        if (!HostAndPort.isValidPort(port)) {
            return false;
        }

        return true;

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
