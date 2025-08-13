package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public class UidFetchers {

    public static final UidFetcher EMPTY = new EmptyUidFetcher();

    public static UidFetcher empty() {
        return EMPTY;
    }

    public static class EmptyUidFetcher implements UidFetcher {
        @Override
        public CompletableFuture<ServiceUid> getServiceUid() {
            return CompletableFuture.completedFuture(ServiceUid.DEFAULT);
        }

        @Override
        public CompletableFuture<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
            return CompletableFuture.failedFuture(new UidException("applicationUid error. name:" + applicationName));
        }
    }
}
