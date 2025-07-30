package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationUidAttrDao {

    ApplicationUidAttribute selectApplicationInfo(ServiceUid serviceUid, ApplicationUid applicationUid);

    boolean insertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute);

    CompletableFuture<Boolean> asyncInsertApplicationNameIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute);

    void deleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    CompletableFuture<Void> asyncDeleteApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid);

    //only for hbase cleanup task
    List<HbaseCellData> selectCellData(ServiceUid serviceUid);
}
