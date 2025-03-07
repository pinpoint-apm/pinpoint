package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServiceGroupService {
    ServiceUid getServiceUid(String serviceName);
}
