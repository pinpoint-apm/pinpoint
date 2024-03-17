/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.util;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * @author HyunGil Jeong
 */
public final class SocketAddressUtils {

    private SocketAddressUtils() {}

    /**
     * Returns the hostname of the given {@link InetSocketAddress}, or the String form of the IP address
     * if it does not have one. Returns {@code null} if neither can be retrieved.
     *
     * <p> This <b>does not</b> trigger a reverse DNS lookup if was created using an address and the hostname
     * is not yet available.
     *
     * @param inetSocketAddress the socket address in which to retrieve the hostname from
     * @return  the hostname or the String representation of the address, or {@code null} if neither
     *          can be found.
     *
     * @see java.net.InetSocketAddress#getHostString()
     */
    public static String getHostNameFirst(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        return inetSocketAddress.getHostString();
    }

    /**
     * Returns the hostname of the given {@link InetSocketAddress}, or the String form of the IP address
     * if it does not have one. Returns {@code defaultHostName} if neither can be retrieved.
     *
     * <p>This <b>does not</b> trigger a reverse DNS lookup if was created using an address and the hostname
     * is not yet available.
     *
     * @param inetSocketAddress the socket address in which to retrieve the hostname from
     * @param defaultHostName   the value to return if neither the hostname nor the string form of the
     *                          address can be found
     * @return  the hostname or the String representation of the address, or the {@code defaultHostName}
     *          if neither can be found.
     *
     * @see java.net.InetSocketAddress#getHostString()
     */
    public static String getHostNameFirst(InetSocketAddress inetSocketAddress, String defaultHostName) {
        String hostName = getHostNameFirst(inetSocketAddress);
        if (hostName == null) {
            return defaultHostName;
        }
        return hostName;
    }

    /**
     * Returns the String form of the IP address of the given {@link InetSocketAddress}, or the hostname
     * if it has not been resolved. Returns {@code null} if neither can be retrieved.
     *
     * <p>This <b>does not</b> trigger a DNS lookup if the address has not been resolved yet by simply
     * returning the hostname.
     *
     * @param inetSocketAddress the socket address in which to retrieve the address from
     * @return  the String representation of the address or the hostname, or {@code null} if neither
     *          can be found.
     *
     * @see InetSocketAddress#isUnresolved()
     */
    public static String getAddressFirst(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return null;
        }
        InetAddress inetAddress = inetSocketAddress.getAddress();
        if (inetAddress != null) {
            return inetAddress.getHostAddress();
        }
        // This won't trigger a DNS lookup as if it got to here, the hostName should not be null on the basis
        // that InetSocketAddress does not allow both address and hostName fields to be null.
        return inetSocketAddress.getHostName();
    }

    /**
     * Returns the String form of the IP address of the given {@link InetSocketAddress}, or the hostname
     * if it has not been resolved. Returns {@code defaultAddress} if neither can be retrieved.
     *
     * <p>This <b>does not</b> trigger a DNS lookup if the address has not been resolved yet by simply
     * returning the hostname.
     *
     * @param inetSocketAddress the socket address in which to retrieve the address from
     * @param defaultAddress    the value to return if neither the string form of the address nor the
     *                          hostname can be found
     * @return  the String representation of the address or the hostname, or {@code defaultAddress} if neither
     *          can be found.
     *
     * @see InetSocketAddress#isUnresolved()
     */
    public static String getAddressFirst(InetSocketAddress inetSocketAddress, String defaultAddress) {
        String address = getAddressFirst(inetSocketAddress);
        if (address == null) {
            return defaultAddress;
        }
        return address;
    }
}
