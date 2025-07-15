package com.navercorp.pinpoint.io.request.supplier;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.io.request.UidException;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ApplicationUidFutureSupplier implements Supplier<ApplicationUid> {
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;

    private final String applicationName;
    private final CompletableFuture<ApplicationUid> applicationUidFuture;
    private final long timeoutSeconds;

    ApplicationUidFutureSupplier(String applicationName, CompletableFuture<ApplicationUid> uid) {
        this(applicationName, uid, DEFAULT_TIMEOUT_SECONDS);
    }

    ApplicationUidFutureSupplier(String applicationName, CompletableFuture<ApplicationUid> uid, long timeoutSeconds) {
        this.applicationName = applicationName;
        this.applicationUidFuture = Objects.requireNonNull(uid, "uid");
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public ApplicationUid get() {
        ApplicationUid applicationUid;
        try {
            applicationUid = applicationUidFuture.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new UidException("Failed to fetch ApplicationUid. name: " + applicationName, e);
        }
        return applicationUid;
    }

}
