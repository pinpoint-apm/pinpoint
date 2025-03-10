package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public class StaticServiceGroupService implements ServiceGroupService {

    private final ServiceUid staticServiceUid;

    public StaticServiceGroupService(int uid) {
        this.staticServiceUid = ServiceUid.of(uid);
    }

    @Override
    public ServiceUid getServiceUid(String serviceName) {
        return staticServiceUid;
    }
}
