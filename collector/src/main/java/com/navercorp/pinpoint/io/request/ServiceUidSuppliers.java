package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidSupplier;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class ServiceUidSuppliers {

    private static final long DEFAULT_TIMEOUT_MILLIS = 1000;

    private ServiceUidSuppliers() {
    }

    public static ServiceUidSupplier newSupplier(String serviceName, UidFetcher uidFetcher) {
        return newSupplier(serviceName, uidFetcher, DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    public static ServiceUidSupplier newSupplier(String serviceName, UidFetcher uidFetcher, long timeout, TimeUnit unit) {
        Objects.requireNonNull(uidFetcher, "uidFetcher");
        Objects.requireNonNull(unit, "unit");

        CompletableFuture<ServiceUid> future;
        try {
            future = Objects.requireNonNull(uidFetcher.getServiceUid(serviceName), "serviceUidFuture");
        } catch (RuntimeException e) {
            future = CompletableFuture.failedFuture(new UidException("Failed to get serviceUid. serviceName:" + serviceName, e));
        }
        return new FutureServiceUidSupplier(serviceName, future, timeout, unit);
    }

    private static class FutureServiceUidSupplier implements ServiceUidSupplier {
        private final String serviceName;
        private final CompletableFuture<ServiceUid> future;
        private final long timeout;
        private final TimeUnit unit;

        private FutureServiceUidSupplier(String serviceName, CompletableFuture<ServiceUid> future, long timeout, TimeUnit unit) {
            this.serviceName = serviceName;
            this.future = Objects.requireNonNull(future, "future");
            this.timeout = timeout;
            this.unit = Objects.requireNonNull(unit, "unit");
        }

        @Override
        public ServiceUid get() {
            return ServiceUidSuppliers.get(serviceName, future, timeout, unit);
        }

        @Override
        public String toString() {
            return "FutureServiceUidSupplier{" +
                    "serviceName='" + serviceName + '\'' +
                    ", serviceUid=" + resolutionState() +
                    '}';
        }

        private String resolutionState() {
            if (!future.isDone()) {
                return "PENDING";
            }
            try {
                return String.valueOf(future.getNow(null));
            } catch (RuntimeException e) {
                return "ERROR";
            }
        }
    }

    private static ServiceUid get(String serviceName, CompletableFuture<ServiceUid> future, long timeout, TimeUnit unit) {
        try {
            ServiceUid serviceUid = future.get(timeout, unit);
            if (serviceUid == null) {
                throw new UidNotFoundException(serviceName);
            }
            return serviceUid;
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
