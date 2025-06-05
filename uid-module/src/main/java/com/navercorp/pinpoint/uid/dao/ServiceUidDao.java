package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ServiceUidDao {

    boolean insertServiceUidIfNotExists(String serviceName, ServiceUid serviceUid);

    List<String> selectAllServiceNames();

    ServiceUid selectServiceUid(String serviceName);

    void deleteServiceUid(String serviceName);
}