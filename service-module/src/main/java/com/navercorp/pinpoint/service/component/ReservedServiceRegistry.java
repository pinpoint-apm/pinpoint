package com.navercorp.pinpoint.service.component;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ReservedServiceRegistry {

    private final Set<String> reservedNames;

    public ReservedServiceRegistry() {
        this.reservedNames = buildReservedNames();
    }

    private static Set<String> buildReservedNames() {
        Set<String> names = new HashSet<>();
        try {
            for (Field field : ServiceUid.class.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == ServiceUid.class) {
                    names.add(field.getName().toUpperCase());
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize reserved service names", e);
        }
        return Set.copyOf(names);
    }

    public boolean contains(String serviceName) {
        if (serviceName == null) {
            return false;
        }
        return reservedNames.contains(serviceName.toUpperCase());
    }

    public Set<String> getReservedNames() {
        return reservedNames;
    }
}
