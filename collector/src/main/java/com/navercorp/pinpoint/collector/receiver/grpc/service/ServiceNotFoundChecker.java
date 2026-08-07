package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.io.request.UidFetcher;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ServiceNotFoundChecker {
    // an unregistered service stays unregistered: throttle against the condition, not the request rate
    private static final Duration UID_LOG_INTERVAL = Duration.ofMinutes(3);

    private final ThrottledLogger uidLogger;

    public ServiceNotFoundChecker(Logger logger) {
        this.uidLogger = newUidLogger(logger);
    }

    public static ThrottledLogger newUidLogger(Logger logger) {
        Objects.requireNonNull(logger, "logger");
        return ThrottledLogger.getUncountedIntervalLogger(logger, UID_LOG_INTERVAL);
    }

    /**
     * Non-blocking snapshot: a pending or failed lookup reads as "not known to be missing".
     */
    public boolean isServiceNotFoundNow(String serviceName, UidFetcher fetcher) {
        try {
            CompletableFuture<ServiceUid> future = fetcher.getServiceUid(serviceName);
            if (!future.isDone() || future.isCompletedExceptionally() || future.isCancelled()) {
                return false;
            }
            if (future.getNow(ServiceUid.UNKNOWN) == null) {
                uidLogger.warn("ServiceUid not found. serviceName={}", serviceName);
                return true;
            }
        } catch (Throwable e) {
            uidLogger.warn("Failed to get serviceUid. serviceName={}", serviceName, e);
        }
        return false;
    }
}