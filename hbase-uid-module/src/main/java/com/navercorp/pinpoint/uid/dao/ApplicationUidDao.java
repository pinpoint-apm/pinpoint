package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.HbaseCellData;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationUidDao {

    ApplicationUid selectApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    CompletableFuture<ApplicationUid> asyncSelectApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    boolean insertApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid);

    CompletableFuture<Boolean> asyncInsertApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid);

    void deleteApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    List<ApplicationUid> selectApplicationUid(ServiceUid serviceUid, String applicationName);

    List<ApplicationUidAttribute> selectApplicationInfo(ServiceUid serviceUid);
    //only for hbase cleanup task
    List<HbaseCellData> selectCellData(ServiceUid serviceUid);
}
