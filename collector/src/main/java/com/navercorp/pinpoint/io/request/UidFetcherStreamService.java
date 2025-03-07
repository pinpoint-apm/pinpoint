package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.SingleEntryUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;

import java.util.Objects;

public class UidFetcherStreamService {

    private final ApplicationUidService applicationUidService;

    public UidFetcherStreamService(ApplicationUidService applicationUidService) {
        this.applicationUidService = Objects.requireNonNull(applicationUidService, "applicationUidService");
    }

    public UidFetcher newUidFetcher() {
        UidCache cache = new SingleEntryUidCacheV1();
        return new UidFetcherV1(applicationUidService, cache);
    }

}
