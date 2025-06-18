package com.navercorp.pinpoint.service.dao;

import com.navercorp.pinpoint.service.vo.ServiceEntry;
import com.navercorp.pinpoint.service.vo.ServiceInfo;

import java.util.List;
import java.util.Map;

public interface ServiceDao {

    int insertService(String name, Map<String, String> configuration);

    List<String> selectServiceNames();

    List<ServiceEntry> selectServiceList(int limit);

    ServiceInfo selectServiceInfo(String name);

    ServiceEntry selectServiceEntry(String name);

    ServiceEntry selectServiceEntry(int uid);

    void updateServiceConfig(int uid, Map<String, String> configuration);

    void updateServiceName(int uid, String name);

    void deleteService(int uid);
}
