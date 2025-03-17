package com.navercorp.pinpoint.collector.uid.service.async;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface AsyncApplicationUidService {
    CompletableFuture<ApplicationUid> getApplicationId(ServiceUid serviceUid, String applicationName);

    CompletableFuture<ApplicationUid> getOrCreateApplicationId(ServiceUid serviceUid, String applicationName);
}
