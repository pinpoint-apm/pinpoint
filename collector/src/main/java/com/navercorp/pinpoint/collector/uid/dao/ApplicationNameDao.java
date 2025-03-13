package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ApplicationNameDao {
//    String selectApplicationName(ServiceUid serviceUid, ApplicationId applicationId);

    boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

}
