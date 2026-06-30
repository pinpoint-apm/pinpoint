package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public final class ServiceUidSuppliers {

    private static final long DEFAULT_TIMEOUT_MILLIS = 1000;

    private ServiceUidSuppliers() {
    }

    public static Supplier<ServiceUid> newSupplier(String serviceName, UidFetcher uidFetcher) {
        return newSupplier(serviceName, uidFetcher, DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    public static Supplier<ServiceUid> newSupplier(String serviceName, UidFetcher uidFetcher, long timeout, TimeUnit unit) {
        Objects.requireNonNull(uidFetcher, "uidFetcher");
        Objects.requireNonNull(unit, "unit");

        if (serviceName == null || serviceName.isEmpty()) {
            return () -> ServiceUid.DEFAULT;
        }
        if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
            return () -> ServiceUid.DEFAULT;
        }
        if (ServiceUid.TEST_SERVICE_UID_NAME.equals(serviceName)) {
            return () -> ServiceUid.TEST_SERVICE;
        }

        CompletableFuture<ServiceUid> future;
        try {
            future = Objects.requireNonNull(uidFetcher.getServiceUid(serviceName), "serviceUidFuture");
        } catch (RuntimeException e) {
            future = CompletableFuture.failedFuture(new UidException("Failed to get serviceUid. serviceName:" + serviceName, e));
        }
        CompletableFuture<ServiceUid> serviceUidFuture = future;
        return () -> get(serviceName, serviceUidFuture, timeout, unit);
    }

    private static ServiceUid get(String serviceName, CompletableFuture<ServiceUid> future, long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UidException("Interrupted while getting serviceUid. serviceName:" + serviceName, e);
        } catch (TimeoutException e) {
            throw new UidException("Timed out while getting serviceUid. serviceName:" + serviceName, e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof UidException uidException) {
                throw uidException;
            }
            throw new UidException("Failed to get serviceUid. serviceName:" + serviceName, cause);
        }
    }
}
