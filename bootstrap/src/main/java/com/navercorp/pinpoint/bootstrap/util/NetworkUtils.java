package com.nhn.pinpoint.bootstrap.util;

import java.net.*;
import java.util.Enumeration;
import java.util.logging.Logger;

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
            Logger.getLogger(NetworkUtils.class.getClass().getName()).warning(e.getMessage());
            hostIp = "127.0.0.1";
        }
        return hostIp;
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

                    final String canonicalHostName = inet.getCanonicalHostName();
                    if (!canonicalHostName.equalsIgnoreCase(inet.getHostAddress())) {
                        name = canonicalHostName;
                        break;
                    }
                }
            }
            return name;
        } catch (SocketException e) {
            Logger.getLogger(NetworkUtils.class.getClass().getName()).warning(e.getMessage());
            return "UNKNOWN-HOST";
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
                // TODO defualt port일 경우 아래와 같이 url을 만들어야 하는지 애매함.
                return host + ":" + port;
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
