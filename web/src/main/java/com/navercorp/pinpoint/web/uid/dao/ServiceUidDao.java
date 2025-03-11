package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServiceUidDao {

    ServiceUid selectServiceUid(String serviceName);

    boolean insertServiceUidIfNotExists(String serviceName, ServiceUid serviceUid);

    void deleteServiceUid(String serviceName);

}
