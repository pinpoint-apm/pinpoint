package com.navercorp.pinpoint.common.server.bo;

import com.navercorp.pinpoint.common.trace.ServiceType;

public final class ServiceTypeUtils {
    private ServiceTypeUtils() {
    }

    public static boolean hasServiceType(final int serviceType) {
        return serviceType != 0 && serviceType != ServiceType.UNDEFINED.getCode();
    }
}
