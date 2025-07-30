package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

import java.util.List;

public interface ApplicationUidService {

    List<ApplicationUidAttribute> getApplicationNames(ServiceUid serviceUid);

    List<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName);

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    ApplicationUidAttribute getApplication(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
