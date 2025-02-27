package com.navercorp.pinpoint.web.dao;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

import java.util.List;

public interface ApplicationUidDao {

    List<ApplicationIdentifier> selectApplicationIds(String applicationName);

    ApplicationUid selectApplicationId(ServiceUid serviceUid, String applicationName);

    void deleteApplicationId(ServiceUid serviceUid, String applicationName);
}
