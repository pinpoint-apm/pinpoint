package com.navercorp.pinpoint.collector.dao;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ApplicationUidDao {

    ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName);

    boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid);

}
