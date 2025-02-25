package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ServiceUidDao {

    ServiceUid selectServiceUid(String serviceName);

    boolean insertServiceUidIfNotExists(String serviceName, ServiceUid serviceUid);

    void deleteServiceUid(String serviceName);

}
