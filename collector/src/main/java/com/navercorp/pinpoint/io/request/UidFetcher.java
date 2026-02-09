package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface UidFetcher {
    CompletableFuture<ServiceUid> getServiceUid(String serviceName);
}
