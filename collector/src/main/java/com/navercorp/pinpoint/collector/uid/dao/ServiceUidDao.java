package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ServiceUidDao {

    ServiceUid selectServiceUid(String serviceName);
}