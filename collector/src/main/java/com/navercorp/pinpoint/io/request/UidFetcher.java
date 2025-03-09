package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.function.Supplier;

public interface UidFetcher {
    Supplier<ServiceUid> getServiceUid();

    Supplier<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName);
}
