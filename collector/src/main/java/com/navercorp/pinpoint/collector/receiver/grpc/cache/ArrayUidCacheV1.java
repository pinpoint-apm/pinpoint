package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.ArrayCache;

public class ArrayUidCacheV1 implements UidCache {

    private final ArrayCache<Key, ApplicationUid> applicationUidCache;

    public ArrayUidCacheV1() {
        this(4);
    }

    public ArrayUidCacheV1(int cacheSize) {
        this.applicationUidCache = new ArrayCache<>(cacheSize);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return ServiceUid.DEFAULT;
    }

    @Override
    public void put(String serviceName, ServiceUid serviceUid) {

    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        return applicationUidCache.get(new Key(serviceUid, applicationName, serviceTypeCode));
    }

    @Override
    public void put(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
        applicationUidCache.put(new Key(serviceUid, applicationName, serviceTypeCode), applicationUid);
    }


    private record Key(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
    }
}
