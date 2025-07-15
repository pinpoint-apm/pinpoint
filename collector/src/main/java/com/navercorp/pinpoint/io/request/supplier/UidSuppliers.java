package com.navercorp.pinpoint.io.request.supplier;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.io.request.UidException;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class UidSuppliers {

    public static Supplier<ApplicationUid> of(String applicationName, ApplicationUid uid) {
        if (UidSuppliers.isUidError(uid)) {
            return new ApplicationUidErrorSupplier(applicationName);
        }
        return new ApplicationUidSupplier(uid);
    }

    public static Supplier<ApplicationUid> of(String applicationName, CompletableFuture<ApplicationUid> future) {
        return new ApplicationUidFutureSupplier(applicationName, future);
    }

    public static Supplier<ApplicationUid> error(String applicationName) {
        return new ApplicationUidErrorSupplier(applicationName);
    }

    public static void throwUidError(ApplicationUid applicationUid) {
        if (isUidError(applicationUid)) {
            throw new UidException("applicationUId error. name:" + applicationUid);
        }
    }

    public static boolean isUidError(ApplicationUid applicationUid) {
        return ApplicationUid.ERROR_APPLICATION_UID.equals(applicationUid);
    }
}
