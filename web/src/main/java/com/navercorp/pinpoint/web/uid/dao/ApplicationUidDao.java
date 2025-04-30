package com.navercorp.pinpoint.web.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ApplicationUidDao {

    List<String> selectApplicationNames(ServiceUid serviceUid);

    ApplicationUid selectApplication(ServiceUid serviceUid, String applicationName);

    void deleteApplicationUid(ServiceUid serviceUid, String applicationName);

    List<HbaseCellData> selectCellData(ServiceUid serviceUid);
}
