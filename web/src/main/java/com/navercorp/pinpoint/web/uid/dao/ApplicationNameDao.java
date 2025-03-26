package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ApplicationNameDao {

    String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);
}
