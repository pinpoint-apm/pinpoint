package com.navercorp.pinpoint.grpc.util;

import io.grpc.Server;

import java.util.concurrent.TimeUnit;

public final class ServerUtils {
    private ServerUtils() {
    }


    public static boolean shutdownAndAwaitTermination(Server server) {
        return shutdownAndAwaitTermination(server, 6000, TimeUnit.MILLISECONDS);
    }

    /**
     * Ref : guava MoreExecutors.shutdownAndAwaitTermination
     */
    public static boolean shutdownAndAwaitTermination(Server server, long timeout, TimeUnit timeUnit) {
        long halfTimeout = timeUnit.toNanos(timeout) / 2;
        server.shutdown();
        try {
            if (!server.awaitTermination(halfTimeout, TimeUnit.NANOSECONDS)) {
                server.shutdownNow();
                server.awaitTermination(halfTimeout, TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
            server.shutdownNow();
        }
        return server.isTerminated();
    }
}
