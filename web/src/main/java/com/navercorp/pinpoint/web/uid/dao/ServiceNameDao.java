package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServiceNameDao {

    String selectServiceName(ServiceUid serviceUid);

    boolean insertServiceNameIfNotExists(ServiceUid serviceUid, String serviceName);

    void deleteServiceName(ServiceUid serviceUid);
}
