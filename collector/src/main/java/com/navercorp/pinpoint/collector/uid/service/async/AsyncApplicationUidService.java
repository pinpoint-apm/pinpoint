package com.navercorp.pinpoint.collector.uid.service.async;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface AsyncApplicationUidService {

    CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName);

    CompletableFuture<ApplicationUid> getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName);
}
