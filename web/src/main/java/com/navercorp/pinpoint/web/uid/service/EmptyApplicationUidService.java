package com.navercorp.pinpoint.web.uid.service;

import com.navercorp.pinpoint.common.server.uid.ApplicationUid;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.util.Collections;
import java.util.List;

public class EmptyApplicationUidService implements ApplicationUidService {

    @Override
    public List<String> getApplicationNames(ServiceUid serviceUid) {
        return Collections.emptyList();
    }

    @Override
    public ApplicationUid getApplicationUid(ServiceUid serviceUid, String applicationName) {
        return null;
    }

    @Override
    public String getApplicationName(ServiceUid serviceUid, ApplicationUid applicationUid) {
        return null;
    }

    @Override
    public void deleteApplication(ServiceUid serviceUid, String applicationName) {

    }
}
