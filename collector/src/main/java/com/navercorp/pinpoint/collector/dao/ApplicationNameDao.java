package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ApplicationNameDao {
//    String selectApplicationName(ServiceUid serviceUid, ApplicationId applicationId);

    boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

}
