package com.navercorp.pinpoint.common.server.uid;

import com.navercorp.pinpoint.common.util.StringUtils;

public class ServiceUidService {


    public static ServiceUid getServiceUid(String serviceName) {
        if (StringUtils.isEmpty(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        if (ServiceUid.DEFAULT_SERVICE_UID_NAME.equals(serviceName)) {
            return ServiceUid.DEFAULT;
        }
        if (ServiceUid.TEST_SERVICE_UID_NAME.equals(serviceName)) {
            return ServiceUid.TEST_SERVICE;
        }
        // TODO ServiceUid query
        return ServiceUid.DEFAULT;
    }
}
