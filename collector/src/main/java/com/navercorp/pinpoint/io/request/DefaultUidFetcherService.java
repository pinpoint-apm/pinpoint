package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.receiver.grpc.cache.ArrayUidCacheV1;
import com.navercorp.pinpoint.collector.receiver.grpc.cache.UidCache;
import com.navercorp.pinpoint.collector.uid.service.ApplicationUidService;

import java.util.Objects;

public class DefaultUidFetcherService implements UidFetcherService {

    private final ApplicationUidService applicationUidCacheService;

    public DefaultUidFetcherService(ApplicationUidService applicationUidCacheService) {
        this.applicationUidCacheService = Objects.requireNonNull(applicationUidCacheService, "applicationUidService");
    }

    @Override
    public UidFetcher newUidFetcher() {
        UidCache cache = new ArrayUidCacheV1();
        return new UidExponentialBackoffFetcherV1(applicationUidCacheService, cache);
    }
}
