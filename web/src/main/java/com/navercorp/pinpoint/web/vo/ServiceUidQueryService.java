package com.navercorp.pinpoint.web.vo;

public class ServiceUidQueryService {

    public static Service getService(int serviceUid) {
        if (Service.DEFAULT.getUid() == serviceUid) {
            return Service.DEFAULT;
        }
        if (Service.TEST_SERVICE.getUid() == serviceUid) {
            return Service.TEST_SERVICE;
        }
        // TODO serviceUid query
        return Service.DEFAULT;
    }
}
