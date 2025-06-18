package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.io.request.supplier.UidSuppliers;

import java.util.function.Supplier;

public class UidFetchers {

    public static final UidFetcher EMPTY = new EmptyUidFetcher();

    public static UidFetcher empty() {
        return EMPTY;
    }

    public static class EmptyUidFetcher implements UidFetcher {
        @Override
        public Supplier<ServiceUid> getServiceUid() {
            return () -> ServiceUid.DEFAULT;
        }

        @Override
        public Supplier<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName) {
            return UidSuppliers.error(applicationName);
        }
    };
}
