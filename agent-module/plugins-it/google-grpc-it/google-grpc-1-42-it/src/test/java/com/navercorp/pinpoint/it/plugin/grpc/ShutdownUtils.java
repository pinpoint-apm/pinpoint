package com.navercorp.pinpoint.it.plugin.grpc;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.TimeUnit;

public final class ShutdownUtils {
    private ShutdownUtils() {
    }

    public static boolean shutdownServer(Server server) {
        if (server == null) {
            return false;
        }
        try {
            return server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public static boolean shutdownEventExecutor(EventExecutorGroup eventExecutors) {
        if (eventExecutors == null) {
            return false;
        }
        try {
            Future<?> future = eventExecutors.shutdownGracefully(500, 500, TimeUnit.MILLISECONDS);
            future.await(3000);
            return future.isSuccess();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    public static boolean shutdownChannel(ManagedChannel channel) {
        if (channel == null) {
            return false;
        }
        try {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

}
