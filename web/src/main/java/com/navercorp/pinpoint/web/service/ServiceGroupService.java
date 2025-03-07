package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ServiceGroupService {

    List<String> selectAllServiceNames();

    String selectServiceName(ServiceUid serviceUid);

    ServiceUid selectServiceUid(String serviceName);

    void createService(String serviceName);

    void deleteService(String serviceName);
}
