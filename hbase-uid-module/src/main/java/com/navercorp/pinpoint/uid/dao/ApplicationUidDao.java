package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;
import com.navercorp.pinpoint.uid.vo.ApplicationUidRow;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationUidDao {

    ApplicationUid getApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    CompletableFuture<ApplicationUid> asyncGetApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    boolean putApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid);

    CompletableFuture<Boolean> asyncPutApplicationUidIfNotExists(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute, ApplicationUid applicationUid);

    void deleteApplicationUid(ServiceUid serviceUid, ApplicationUidAttribute applicationUidAttribute);

    List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid);

    List<ApplicationUidRow> scanApplicationUidRow(ServiceUid serviceUid, String applicationName);
}
