package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface ApplicationUidService {

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName);

    ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName);

    CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName);
}
