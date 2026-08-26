package com.navercorp.pinpoint.service.service;

import com.navercorp.pinpoint.common.server.uid.Service;

import java.util.List;

public interface ServiceRegistryService {

    Service insertService(String name);

    List<String> getServiceNames();

    List<Service> getServiceList(int limit);

    Service getService(String name);

    Service getService(int uid);

    void deleteService(String name);
}
