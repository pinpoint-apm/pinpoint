package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ServiceNameDao {

    List<String> selectAllServiceNames();

    String selectServiceName(ServiceUid serviceUid);

    boolean insertServiceNameIfNotExists(ServiceUid serviceUid, String serviceName);

    void deleteServiceName(ServiceUid serviceUid);
}
