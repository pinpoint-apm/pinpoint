package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.service.vo.ServiceEntity;

import java.util.List;

public interface ServiceRegistryService {

    ServiceEntity insertService(String name);

    List<String> getServiceNames();

    ServiceEntity getService(String name);

    ServiceEntity getService(int uid);

    void deleteService(String name);
}
