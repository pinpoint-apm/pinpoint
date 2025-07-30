package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BaseApplicationUidService {

    List<ApplicationUid> getApplicationUid(ServiceUid serviceUid, String applicationName);

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    List<ApplicationUidAttribute> getApplications(ServiceUid serviceUid);

    ApplicationUidAttribute getApplication(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
