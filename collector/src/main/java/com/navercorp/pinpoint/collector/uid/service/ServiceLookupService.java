package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface ServiceLookupService {

    /**
     * Resolve a single serviceName to its serviceUid. The future completes with null when the serviceName is not
     * registered.
     */
    CompletableFuture<ServiceUid> getServiceUid(String serviceName);
}
