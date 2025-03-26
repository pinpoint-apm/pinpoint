package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ServiceUidDao {

    List<String> selectAllServiceNames();

    ServiceUid selectServiceUid(String serviceName);

    boolean insertServiceUidIfNotExists(String serviceName, ServiceUid serviceUid);

    void deleteServiceUid(String serviceName);

}
