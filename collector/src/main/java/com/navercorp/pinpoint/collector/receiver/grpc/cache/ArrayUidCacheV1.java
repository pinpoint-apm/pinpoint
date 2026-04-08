package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.util.ArrayCache;

public class ArrayUidCacheV1 implements UidCache {

    private final ArrayCache<String, ServiceUid> serviceUidCache;

    public ArrayUidCacheV1() {
        this(4);
    }

    public ArrayUidCacheV1(int cacheSize) {
        this.serviceUidCache = new ArrayCache<>(cacheSize);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return serviceUidCache.get(serviceName);
    }

    @Override
    public void put(String serviceName, ServiceUid serviceUid) {
        serviceUidCache.put(serviceName, serviceUid);
    }

}