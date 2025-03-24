package com.navercorp.pinpoint.web.service.component;

public class DisableAgentCompatibility implements LegacyAgentCompatibility {
    @Override
    public boolean isLegacyAgent(int serviceType) {
        return false;
    }

    @Override
    public boolean isLegacyAgent(int serviceType, String version) {
        return false;
    }
}
