package com.navercorp.pinpoint.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Deprecated
public interface BaseApplicationUidService {

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    ApplicationUidAttribute getApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName, int serviceTypeCode);

    List<ApplicationUidRow> getApplications(ServiceUid serviceUid);

    List<ApplicationUidRow> getApplications(ServiceUid serviceUid, String applicationName);

    CompletableFuture<ApplicationUid> asyncGetOrCreateApplicationUid(ServiceUid serviceUid, String applicationName, int serviceTypeCode);
}
