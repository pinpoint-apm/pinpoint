package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUidService;

import java.util.concurrent.CompletableFuture;

public class UidFetchers {

    public static final UidFetcher DEFAULT_UID_FETCHER = new DefaultUidFetcher();

    public static UidFetcher defaultUidFetcher() {
        return DEFAULT_UID_FETCHER;
    }

    public static class DefaultUidFetcher implements UidFetcher {
        @Override
        public CompletableFuture<ServiceUid> getServiceUid(String serviceName) {
            ServiceUid serviceUid = ServiceUidService.getServiceUid(serviceName);
            return CompletableFuture.completedFuture(serviceUid);
        }
    }
}
