package com.navercorp.pinpoint.alarm.web;

import java.util.Objects;

final class AlarmServiceNames {

    private AlarmServiceNames() {
    }

    static boolean isSame(String currentServiceName, String requestServiceName) {
        return Objects.equals(currentServiceName, requestServiceName);
    }
}
