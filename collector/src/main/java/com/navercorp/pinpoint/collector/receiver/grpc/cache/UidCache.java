package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface UidCache {

    ServiceUid getServiceUid(String serviceName);

    void put(String serviceName, ServiceUid serviceUid);

}
