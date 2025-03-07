package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public interface ApplicationUidService {

    ApplicationUid getApplicationId(ServiceUid serviceUid, String applicationName);

    ApplicationUid getOrCreateApplicationId(ServiceUid serviceUid, String applicationName);

}
