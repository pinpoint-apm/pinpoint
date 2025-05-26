package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationNameDao {

    String selectApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, String applicationName);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    //only for hbase cleanup task
    List<HbaseCellData> selectCellData(ServiceUid serviceUid);
}
