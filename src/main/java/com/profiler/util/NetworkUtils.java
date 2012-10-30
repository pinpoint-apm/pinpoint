package com.profiler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 *
 */
public class NetworkUtils {

    public static String getMachineName() {
        try {
            String name = null;
            Enumeration<NetworkInterface> enet = NetworkInterface.getNetworkInterfaces();

            while (enet.hasMoreElements() && (name == null)) {
                NetworkInterface net = enet.nextElement();

                if (net.isLoopback())
                    continue;

                Enumeration<InetAddress> eaddr = net.getInetAddresses();

                while (eaddr.hasMoreElements()) {
                    InetAddress inet = eaddr.nextElement();

                    if (inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress()) == false) {
                        name = inet.getCanonicalHostName();
                        break;
                    }
                }
            }
            return name;
        } catch (Exception e) {
            Logger.getLogger(NetworkUtils.class.getClass().getName()).warning(e.getMessage());
            return "UNKNOWN-HOST";
        }
    }
}
