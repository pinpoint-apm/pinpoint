package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

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
