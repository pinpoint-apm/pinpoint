package com.nhn.pinpoint.profiler.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author emeroad
 */
public class LocalHostTest {

    public static void main(String[] args) throws UnknownHostException, SocketException {
        System.out.println("Canonical:" + InetAddress.getLocalHost().getCanonicalHostName());
        System.out.println("normal:" + InetAddress.getLocalHost().getHostName());

        System.out.println("NetworkInterface");
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while(networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            System.out.println("Nic:" + networkInterface);
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while(inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                System.out.println(inetAddress.getCanonicalHostName());
                System.out.println(inetAddress.getHostName());
            }
        }
    }

}
