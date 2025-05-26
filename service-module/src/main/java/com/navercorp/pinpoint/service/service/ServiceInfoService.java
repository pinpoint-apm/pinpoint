package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.service.vo.ServiceInfo;

import java.util.List;
import java.util.Map;

public interface ServiceInfoService {

    int insertService(String serviceName, Map<String, String> configuration);

    List<String> getServiceNames();

    ServiceInfo getServiceInfo(String serviceName);

    ServiceUid getServiceUid(String serviceName);

    String getServiceName(ServiceUid serviceUid);

    void updateServiceConfig(String serviceName, Map<String, String> newConfiguration);

    void updateServiceName(String serviceName, String newServiceName);

    void deleteService(String serviceName);
}
