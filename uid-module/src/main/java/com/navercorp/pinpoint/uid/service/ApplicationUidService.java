package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;

public interface ApplicationUidService {

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName);

    ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName);

    List<String> getApplicationNames(ServiceUid serviceUid);

    String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName);
}
