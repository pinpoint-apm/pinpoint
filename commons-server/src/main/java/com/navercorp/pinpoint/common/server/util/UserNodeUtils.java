package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.trace.ServiceType;

public final class UserNodeUtils {

    private UserNodeUtils() {
    }

    public static String newUserNodeName(String applicationName, ServiceType applicationServiceType) {
        return applicationName + '_' + applicationServiceType.getName();
    }
}
