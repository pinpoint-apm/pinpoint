package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public class UidFetchers {

    public static final UidFetcher DEFAULT_UID_FETCHER = new DefaultUidFetcher();

    public static UidFetcher defaultUidFetcher() {
        return DEFAULT_UID_FETCHER;
    }

    public static class DefaultUidFetcher implements UidFetcher {
        @Override
        public CompletableFuture<ServiceUid> getServiceUid(String serviceName) {
            if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
                return CompletableFuture.completedFuture(ServiceUid.DEFAULT);
            }
            return CompletableFuture.failedFuture(new UidException("Unsupported serviceName:" + serviceName));
        }
    }
}
