/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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
package com.navercorp.pinpoint.plugin.jboss.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.jboss.remoting3.Connection;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * The Class JbossUtility.
 *
 * @author <a href="mailto:suraj.raturi89@gmail.com">Suraj Raturi</a>
 */
public class JbossUtility {

    /** The Constant LOGGER. */
    private static final PLogger LOGGER = PLoggerFactory.getLogger(JbossUtility.class);

    /**
     * Fetch remote address.
     *
     * @param connection the connection
     * @return the string
     */
    public static String fetchRemoteAddress(final Connection connection) {
        if (connection == null) {
            return "";
        }
        final List<String> remoteAddressInfos = new ArrayList<String>();
        for (final Principal principal : connection.getPrincipals()) {
            if (principal instanceof org.jboss.remoting3.security.InetAddressPrincipal) {
                final InetAddress inetAddress = ((org.jboss.remoting3.security.InetAddressPrincipal) principal).getInetAddress();
                initializeRemoteAddressInfo(remoteAddressInfos, inetAddress);
                break;
            }
        }
        return remoteAddressInfos.toString();
    }

    /**
     * Initialize remote address info.
     *
     * @param remoteAddressInfos the remote address infos
     * @param inetAddress the inet address
     */
    @SuppressWarnings("static-access")
    private static void initializeRemoteAddressInfo(final List<String> remoteAddressInfos, final InetAddress inetAddress) {
        if (inetAddress == null) {
            return;
        }
        try {
            final NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);
            final Enumeration<NetworkInterface> networkInterfaceEnum = networkInterface.getNetworkInterfaces();
            while (networkInterfaceEnum.hasMoreElements()) {
                final NetworkInterface nextNetworkInterfaceElement = networkInterfaceEnum.nextElement();
                final Enumeration<InetAddress> inetAddressEnum = nextNetworkInterfaceElement.getInetAddresses();
                while (inetAddressEnum.hasMoreElements()) {
                    final InetAddress nextInetAddressElement = inetAddressEnum.nextElement();
                    remoteAddressInfos.add(nextInetAddressElement.getHostAddress());
                }
            }
        } catch (final Exception exception) {
            LOGGER.error("An error occurred while searching for a network interface that has specified address bound to it - {}" + inetAddress, exception);
        }
    }

    /**
     * Fetch remote address details.
     *
     * @param remoteAddress the remote address
     * @return the string
     */
    public static String fetchRemoteAddressDetails(final String remoteAddress) {
        final List<String> remoteAddressInfos = new ArrayList<String>();
        InetAddress inetAddress = null;
        try {
            // TODO check DNSLookup risk
            inetAddress = InetAddress.getByName(remoteAddress);
        } catch (final UnknownHostException unknownHostException) {
            LOGGER.error("An error occurred while fetching ip address from host name - {}", remoteAddress, unknownHostException);
            return remoteAddress;
        }
        initializeRemoteAddressInfo(remoteAddressInfos, inetAddress);
        return remoteAddressInfos.toString();
    }
}
