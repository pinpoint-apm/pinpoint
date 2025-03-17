package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface ApplicationUidDao {

    ApplicationUid selectApplicationUid(ServiceUid serviceUid, String applicationName);

    CompletableFuture<ApplicationUid> asyncSelectApplicationUid(ServiceUid serviceUid, String applicationName);

    boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid);

    CompletableFuture<Boolean> asyncInsertApplicationUidIfNotExists(ServiceUid serviceUid, String applicationName, ApplicationUid applicationUid);
}
