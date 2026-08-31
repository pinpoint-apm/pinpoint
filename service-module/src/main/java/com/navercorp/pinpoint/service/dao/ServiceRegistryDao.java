package com.navercorp.pinpoint.service.dao;

import com.navercorp.pinpoint.service.dao.dto.ServiceEntity;

import java.util.List;

public interface ServiceRegistryDao {

    int insertService(int uid, String name);

    List<String> selectServiceNames();

    List<ServiceEntity> selectServiceList(int limit);

    ServiceEntity selectServiceByName(String name);

    ServiceEntity selectServiceByUid(int uid);

    void deleteService(int uid);
}
