package com.navercorp.pinpoint.web.dao;

import java.util.UUID;

public interface ServiceUidDao {

    UUID selectServiceUid(String serviceName);

    boolean insertServiceUidIfNotExists(String serviceName, UUID serviceUid);

    void deleteServiceUid(String serviceName);

}
