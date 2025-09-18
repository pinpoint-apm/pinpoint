package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServiceUidService {
    ServiceUid getServiceUid(String serviceName);

    String getServiceName(ServiceUid serviceUid);
}
