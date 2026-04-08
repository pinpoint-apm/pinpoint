package com.navercorp.pinpoint.service.dao;

import com.navercorp.pinpoint.service.vo.ServiceEntity;

import java.util.List;

public interface ServiceRegistryDao {

    int insertService(int uid, String name);

    List<String> selectServiceNames();

    List<ServiceEntity> selectServiceList(int limit);

    ServiceEntity selectService(String name);

    ServiceEntity selectService(int uid);

    void deleteService(int uid);
}
