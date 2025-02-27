package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.common.server.vo.ApplicationUid;
import com.navercorp.pinpoint.common.server.vo.ServiceUid;

public interface ApplicationUidService {

    ApplicationUid getApplicationId(ServiceUid serviceUid, String applicationName);

    ApplicationUid getOrCreateApplicationId(ServiceUid serviceUid, String applicationName);

}
