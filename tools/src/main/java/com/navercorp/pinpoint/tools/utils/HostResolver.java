package com.navercorp.pinpoint.tools.utils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
public final class HostResolver {

    public static List<InetSocketAddress> getIPList(InetSocketAddress address) throws UnknownHostException {
        String host = address.getHostName();

        InetAddress[] resolvedAddresses = InetAddress.getAllByName(host);

        List<InetSocketAddress> resolvedAddressList = new ArrayList<InetSocketAddress>(resolvedAddresses.length);

        for (InetAddress resolvedAddress : resolvedAddresses) {
            resolvedAddressList.add(new InetSocketAddress(resolvedAddress.getHostAddress(), address.getPort()));
        }

        return resolvedAddressList;
    }

}
