package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ServiceUidDao {

    ServiceUid selectServiceUid(String serviceName);
}