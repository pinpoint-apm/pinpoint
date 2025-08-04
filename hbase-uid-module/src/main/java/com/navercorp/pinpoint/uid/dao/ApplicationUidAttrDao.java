package com.navercorp.pinpoint.uid.dao;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttrRow;
import com.navercorp.pinpoint.uid.vo.ApplicationUidAttribute;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ApplicationUidAttrDao {

    ApplicationUidAttribute getApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid);

    boolean putApplicationAttrIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute);

    CompletableFuture<Boolean> asyncPutApplicationAttrIfNotExists(ServiceUid serviceUid, ApplicationUid applicationUid, ApplicationUidAttribute applicationUidAttribute);

    void deleteApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid);

    CompletableFuture<Void> asyncDeleteApplicationAttr(ServiceUid serviceUid, ApplicationUid applicationUid);

    //only for hbase cleanup task
    List<ApplicationUidAttrRow> scanApplicationAttrRow(ServiceUid serviceUid);
}
