package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.SingleEntryUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.CachedApplicationUidService;

import java.util.Objects;

public class UidFetcherStreamService {

    private final CachedApplicationUidService cachedApplicationUidService;

    public UidFetcherStreamService(CachedApplicationUidService cachedApplicationUidService) {
        this.cachedApplicationUidService = Objects.requireNonNull(cachedApplicationUidService, "applicationUidService");
    }

    public UidFetcher newUidFetcher() {
        UidCache cache = new SingleEntryUidCacheV1();
        return new UidFetcherV1(cachedApplicationUidService, cache);
    }

}
