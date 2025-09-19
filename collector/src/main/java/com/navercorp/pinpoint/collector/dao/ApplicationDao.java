package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ApplicationDao {

    void insert(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
