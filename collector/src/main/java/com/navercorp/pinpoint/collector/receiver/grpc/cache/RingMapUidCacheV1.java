package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.RingMapCache;

public class RingMapUidCacheV1 implements UidCache {

    private final RingMapCache<Key, ApplicationUid> applicationUidCache;

    public RingMapUidCacheV1(int applicationUidCacheSize) {
        this.applicationUidCache = new RingMapCache<>(applicationUidCacheSize);
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
        applicationUidCache.putIfAbsent(new Key(serviceUid, applicationName, serviceTypeCode), applicationUid);
    }

    private record Key(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
    }
}
