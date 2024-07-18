package com.navercorp.pinpoint.collector.receiver.grpc.keepalive;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class KeepAliveRegistry implements Closeable {

    private static final Metadata.Key<String> KEEPALIVE = Metadata.Key.of("stream-keepalive", Metadata.ASCII_STRING_MARSHALLER);

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ConcurrentMap<ServerCall<?, ?>, LastPacketTimeCapture> map = new ConcurrentHashMap<>();

    private final Timer timer = new Timer("KeepAliveRegistry-Timer", true);

    private final long keepAliveTimeOutMs;

    public KeepAliveRegistry() {
        this(10_000, 10_000);
    }

    public KeepAliveRegistry(long keepAliveTimeOutMs, long period) {
        this.keepAliveTimeOutMs = keepAliveTimeOutMs;
        timer.schedule(new LastCheckTask(), period, period);
    }

    public LastPacketTimeCapture put(ServerCall<?, ?> call, LastPacketTimeCapture updater) {
        Objects.requireNonNull(call, "call");
        return this.map.put(call, updater);
    }

    public LastPacketTimeCapture remove(ServerCall<?, ?> call) {
        Objects.requireNonNull(call, "call");
        return this.map.remove(call);
    }

    public class LastCheckTask extends TimerTask {
        public LastCheckTask() {
        }

        @Override
        public void run() {
            final ConcurrentMap<ServerCall<?, ?>, LastPacketTimeCapture> copy = map;
            final int size = copy.size();
            logger.debug("LastCheckTask stream size:{}", size);
            if (size == 0 ) {
                return;
            }
            logger.debug("last packet check start");
            List<ServerCall<?, ?>> timeoutCalls = new ArrayList<>();
            List<ServerCall<?, ?>> canceledCalls = new ArrayList<>();
            long current = System.currentTimeMillis();
            for (Map.Entry<ServerCall<?, ?>, LastPacketTimeCapture> entry : copy.entrySet()) {
                final ServerCall<?, ?> call = entry.getKey();
                if (call.isCancelled()) {
                    canceledCalls.add(call);
                    continue;
                }

                final LastPacketTimeCapture lastPacketTimeCapture = entry.getValue();
                long ago = current - lastPacketTimeCapture.last();
                if (ago > keepAliveTimeOutMs) {
                    timeoutCalls.add(call);
                }
            }

            canceledCalls(copy, canceledCalls);
            streamTimeout(copy, timeoutCalls);
        }

        private void canceledCalls(ConcurrentMap<ServerCall<?, ?>, LastPacketTimeCapture> map, List<ServerCall<?, ?>> canceledCalls) {
            if (canceledCalls.isEmpty()) {
                return;
            }
            logger.warn("ServerCall already cancelled size:{}", canceledCalls.size());
            for (ServerCall<?, ?> canceledCall : canceledCalls) {
                map.remove(canceledCall);
            }
        }

        private void streamTimeout(ConcurrentMap<ServerCall<?, ?>, LastPacketTimeCapture> map, List<ServerCall<?, ?>> timeoutCalls) {
            if (timeoutCalls.isEmpty()) {
                return;
            }
            logger.warn("Force timeout ServerCall.close() size:{} timeout:{}ms", timeoutCalls.size(), keepAliveTimeOutMs);
            for (ServerCall<?, ?> timeoutCall : timeoutCalls) {
                map.remove(timeoutCall);
            }

            Metadata trailers = new Metadata();
            trailers.put(KEEPALIVE, "stream-timeout");
            Status status = Status.CANCELLED.withDescription("Canceled-by-server");
            for (ServerCall<?, ?> serverCall : timeoutCalls) {
                if (serverCall.isCancelled()) {
                    continue;
                }
                try {
                    serverCall.close(status, trailers);
                } catch (Throwable throwable) {
                    logger.info("Error closing ServerCall", throwable);
                }
            }
        }
    }

    @Override
    public void close() {
        this.logger.debug("close");

        this.map.clear();
        this.timer.cancel();
        this.timer.purge();
    }

    @Override
    public String toString() {
        return "KeepAliveRegistry{" +
                "keepAliveTimeOutMs=" + keepAliveTimeOutMs +
                '}';
    }
}
