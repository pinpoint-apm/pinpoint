package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface UidFetcher {
    CompletableFuture<ServiceUid> getServiceUid();

    CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
