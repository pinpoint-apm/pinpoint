package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public class EmptyApplicationUidService implements ApplicationUidService {
    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return null;
    }

    @Override
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return null;
    }

    @Override
    public CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return CompletableFuture.completedFuture(null);
    }
}
