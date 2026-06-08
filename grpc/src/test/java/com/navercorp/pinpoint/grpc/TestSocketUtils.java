package com.navercorp.pinpoint.grpc;

import javax.net.ServerSocketFactory;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Random;

public final class TestSocketUtils {

    public static final int PORT_RANGE_MIN = 1024;

    public static final int PORT_RANGE_MAX = 65535;

    private static final int MAX_ATTEMPTS = 100;

    private static final Random RANDOM = new Random(System.nanoTime());

    public TestSocketUtils() {
    }

    public static int findAvailableTcpPort() {
        return findAvailableTcpPort(PORT_RANGE_MIN, PORT_RANGE_MAX);
    }

    public static int findAvailableTcpPort(int minPort, int maxPort) {
        int portRange = maxPort - minPort + 1;
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            int port = minPort + RANDOM.nextInt(portRange);
            if (isPortAvailable(port)) {
                return port;
            }
        }
        throw new IllegalStateException("no free port in range [" + minPort + ", " + maxPort + "]");
    }

    static boolean isPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault()
                    .createServerSocket(port, 1, InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}