package com.navercorp.pinpoint.io.request.supplier;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;

import java.util.Objects;
import java.util.function.Supplier;

public class ApplicationUidSupplier implements Supplier<ApplicationUid> {

    private final ApplicationUid uid;

    ApplicationUidSupplier(ApplicationUid uid) {
        this.uid = Objects.requireNonNull(uid, "uid");
    }

    @Override
    public ApplicationUid get() {
        return uid;
    }

}
