package com.navercorp.pinpoint.io.request.supplier;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.io.request.UidException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ApplicationUidBackOffSupplier implements Supplier<ApplicationUid> {
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    private final String applicationName;
    private final Supplier<CompletableFuture<ApplicationUid>> futureSupplier;
    private final long nextAllowedTime;

    private CompletableFuture<ApplicationUid> applicationUidFuture;

    public ApplicationUidBackOffSupplier(String applicationName, Supplier<CompletableFuture<ApplicationUid>> futureSupplier, long nextAllowedTime) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
        this.futureSupplier = Objects.requireNonNull(futureSupplier, "futureSupplier");
        this.nextAllowedTime = nextAllowedTime;
        if (this.nextAllowedTime <= 0) {
            this.applicationUidFuture = futureSupplier.get();
        }
    }

    @Override
    public ApplicationUid get() {
        if (nextAllowedTime > 0 && nextAllowedTime > System.currentTimeMillis()) {
            throw new UidException("BackOff in progress. name: " + applicationName);
        }
        if (applicationUidFuture == null) {
            applicationUidFuture = futureSupplier.get();
        }

        try {
            return applicationUidFuture.get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new UidException("Failed to fetch ApplicationUid. name: " + applicationName, e);
        }
    }

}
