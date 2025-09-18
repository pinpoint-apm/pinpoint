package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

@Deprecated
public interface ApplicationUidService {

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
