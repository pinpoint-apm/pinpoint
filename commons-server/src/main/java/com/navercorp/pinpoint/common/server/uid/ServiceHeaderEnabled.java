package com.navercorp.pinpoint.common.server.uid;

public class ServiceHeaderEnabled {

    public static final String ENABLED = "pinpoint.modules.service-header.enabled";

    private final boolean enabled;

    public ServiceHeaderEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String toString() {
        return "ServiceHeaderEnabled{" + ENABLED + '=' + enabled + '}';
    }
}
