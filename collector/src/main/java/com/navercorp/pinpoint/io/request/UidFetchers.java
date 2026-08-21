package com.navercorp.pinpoint.io.request;

import com.navercorp.pinpoint.collector.uid.service.StaticServiceLookupService;

public class UidFetchers {

    private static final StaticServiceLookupService STATIC_SERVICE_LOOKUP_SERVICE = new StaticServiceLookupService();

    public static final UidFetcher DEFAULT_UID_FETCHER = STATIC_SERVICE_LOOKUP_SERVICE::getServiceUid;

    public static UidFetcher defaultUidFetcher() {
        return DEFAULT_UID_FETCHER;
    }
}
