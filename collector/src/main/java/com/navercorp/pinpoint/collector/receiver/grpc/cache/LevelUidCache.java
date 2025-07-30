package com.navercorp.pinpoint.collector.receiver.grpc.cache;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Objects;

public class LevelUidCache implements UidCache {

    private final UidCache l1;
    private final UidCache l2;

    public LevelUidCache(UidCache l1, UidCache l2) {
        this.l1 = Objects.requireNonNull(l1, "l1");
        this.l2 = Objects.requireNonNull(l2, "l2");
    }

    public ServiceUid getServiceUid(String serviceName) {
        final ServiceUid hit1 = l1.getServiceUid(serviceName);
        if (hit1 != null) {
            return hit1;
        }
        return l2.getServiceUid(serviceName);
    }

    public void put(String serviceName, ServiceUid serviceUid) {
        l1.put(serviceName, serviceUid);
        l2.put(serviceName, serviceUid);
    }


    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode) {
        final ApplicationUid hit1 = l1.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
        if (hit1 != null) {
            return hit1;
        }
        return l2.getApplicationUid(serviceUid, applicationName, serviceTypeCode);
    }

    public void put(ServiceUid serviceUid, String applicationName, int serviceTypeCode, ApplicationUid applicationUid) {
        l1.put(serviceUid, applicationName, serviceTypeCode, applicationUid);
        l2.put(serviceUid, applicationName, serviceTypeCode, applicationUid);
    }

}
