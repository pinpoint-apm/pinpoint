package com.navercorp.pinpoint.web.dao;

import java.util.List;
import java.util.UUID;

public interface ServiceNameDao {

    List<String> selectAllServiceNames();

    String selectServiceName(UUID serviceUid);

    boolean insertServiceNameIfNotExists(UUID serviceUid, String serviceName);

    void deleteServiceName(UUID serviceUid);
}
