package com.navercorp.pinpoint.service.component;

import com.navercorp.pinpoint.common.server.uid.Service;

import java.util.HashSet;
import java.util.Set;

public class ReservedServiceRegistry {

    // ServiceUid.NULL is an in-memory sentinel, not a service name, but the name stays reserved to avoid confusion
    private static final String NULL_SERVICE_NAME = "NULL";

    private static final Set<String> RESERVED_NAMES = buildReservedNames();

    private static Set<String> buildReservedNames() {
        Set<String> names = new HashSet<>();
        for (Service service : Service.wellKnownServices()) {
            names.add(service.getServiceName());
        }
        names.add(NULL_SERVICE_NAME);
        return Set.copyOf(names);
    }

    public boolean contains(String serviceName) {
        if (serviceName == null) {
            return false;
        }
        String upperCase = serviceName.toUpperCase();
        if (Service.wellKnownService(upperCase) != null) {
            return true;
        }
        return NULL_SERVICE_NAME.equals(upperCase);
    }

    public Set<String> getReservedNames() {
        return RESERVED_NAMES;
    }
}
