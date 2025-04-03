package com.navercorp.pinpoint.collector.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

public class EmptyApplicationUidService implements ApplicationUidService {

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return null;
    }

    @Override
    public ApplicationUid getOrCreateApplicationUid(ServiceUid serviceUid, String applicationName) {
        return null;
    }
}
