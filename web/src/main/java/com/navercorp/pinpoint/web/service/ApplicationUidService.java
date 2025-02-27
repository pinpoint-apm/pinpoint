package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ApplicationIdentifier;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

import java.util.List;

public interface ApplicationUidService {

    List<ApplicationIdentifier> getApplicationIds(String applicationName);

    List<String> getApplicationNames(ServiceUid serviceUid);

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName);

    String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName);
}
