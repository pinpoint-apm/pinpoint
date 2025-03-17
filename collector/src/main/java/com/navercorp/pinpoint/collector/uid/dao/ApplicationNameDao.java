package com.navercorp.pinpoint.collector.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.concurrent.CompletableFuture;

public interface ApplicationNameDao {

    boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);
}
