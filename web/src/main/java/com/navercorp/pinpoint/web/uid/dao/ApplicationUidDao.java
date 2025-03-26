package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ApplicationUidDao {

    List<String> selectApplicationUidRows(ServiceUid serviceUid);

    ApplicationUid selectApplication(ServiceUid serviceUid, String applicationName);

    void deleteApplicationUid(ServiceUid serviceUid, String applicationName);
}
