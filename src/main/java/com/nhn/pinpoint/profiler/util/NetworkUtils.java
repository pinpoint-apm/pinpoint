package com.nhn.pinpoint.profiler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 *
 */
public final class NetworkUtils {

    private NetworkUtils() {
    }

    public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "UNKNOWN-HOST";
		}
	}
	
	@Deprecated
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

    public static String getHostFromURL(String url) {
        try {
            URL u = new URL(url);

            String host = u.getHost();
            int port = u.getPort();

            if (port > 0) {
                return host + ":" + port;
            } else {
                return host;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
