package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.SingleEntryUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;

import java.util.Objects;

public class UidFetcherStreamService {

    private final ApplicationUidService applicationUidCacheService;

    public UidFetcherStreamService(ApplicationUidService applicationUidCacheService) {
        this.applicationUidCacheService = Objects.requireNonNull(applicationUidCacheService, "applicationUidService");
    }

    public UidFetcher newUidFetcher() {
        UidCache cache = new SingleEntryUidCacheV1();
        return new UidFetcherV1(applicationUidCacheService, cache);
    }

}
