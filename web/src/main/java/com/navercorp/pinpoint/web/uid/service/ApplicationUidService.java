package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import jakarta.annotation.Nullable;

import java.util.List;

public interface ApplicationUidService {

    List<String> getApplicationNames(ServiceUid serviceUid);

    ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName);

    String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    void deleteApplication(ServiceUid serviceUid, String applicationName);

    int cleanupEmptyApplication(@Nullable ServiceUid serviceUid, long fromTimestamp);

    int cleanupInconsistentApplicationName(@Nullable ServiceUid serviceUid);
}
