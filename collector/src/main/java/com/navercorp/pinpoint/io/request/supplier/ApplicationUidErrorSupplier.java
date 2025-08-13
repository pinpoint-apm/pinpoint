package com.navercorp.pinpoint.io.request.supplier;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.io.request.UidException;

import java.util.Objects;
import java.util.function.Supplier;

public class ApplicationUidErrorSupplier implements Supplier<ApplicationUid> {

    private final String applicationName;

    ApplicationUidErrorSupplier(String applicationName) {
        this.applicationName = Objects.requireNonNull(applicationName, "applicationName");
    }

    @Override
    public ApplicationUid get() {
        throw new UidException("applicationUid error. name:" + applicationName);
    }

}
