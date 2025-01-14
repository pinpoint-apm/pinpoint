package com.navercorp.pinpoint.web.service;

import java.util.List;
import java.util.UUID;

public interface ServiceGroupService {

    void createService(String serviceName);
    void deleteService(String serviceName);

    List<String> selectAllServiceNames();
    String selectServiceName(UUID serviceUid);
    UUID selectServiceUid(String serviceName);

}
