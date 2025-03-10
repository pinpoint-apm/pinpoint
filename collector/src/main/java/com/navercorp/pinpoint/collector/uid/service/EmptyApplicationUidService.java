package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public class EmptyApplicationUidService implements ApplicationUidService {

    @Override
    public ApplicationUid getApplicationId(ServiceUid serviceUid, String applicationName) {
        return null;
    }

    @Override
    public ApplicationUid getOrCreateApplicationId(ServiceUid serviceUid, String applicationName) {
        return null;
    }
}
