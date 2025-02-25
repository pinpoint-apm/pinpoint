package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;

import java.util.List;
import java.util.UUID;

public interface ServiceNameDao {

    List<String> selectAllServiceNames();

    String selectServiceName(ServiceUid serviceUid);

    boolean insertServiceNameIfNotExists(ServiceUid serviceUid, String serviceName);

    void deleteServiceName(ServiceUid serviceUid);
}
